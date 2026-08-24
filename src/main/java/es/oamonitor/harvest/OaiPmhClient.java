package es.oamonitor.harvest;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.kb.oai.pmh.ErrorResponseException;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.ResumptionToken;

import java.util.ArrayList;
import java.util.List;

@Component
public class OaiPmhClient {

    private static final Logger log = LoggerFactory.getLogger(OaiPmhClient.class);

    public OaiPage listRecords(String endpoint, String from, String until, String metadataPrefix, ResumptionToken resumptionToken) {
        try {
            OaiPmhServer server = new OaiPmhServer(endpoint);
            RecordsList recordsList;

            if (resumptionToken != null) {
                recordsList = server.listRecords(resumptionToken);
            } else {
                recordsList = server.listRecords(metadataPrefix, from, until, null);
            }

            List<OaiRecord> records = new ArrayList<>();
            for (Object obj : recordsList.asList()) {
                if (obj instanceof Record) {
                    Record record = (Record) obj;
                    OaiRecord oaiRecord = parseRecord(record);
                    if (oaiRecord != null) {
                        records.add(oaiRecord);
                    }
                }
            }

            ResumptionToken nextResumptionToken = recordsList.getResumptionToken();

            return new OaiPage(records, nextResumptionToken);

        } catch (ErrorResponseException e) {
            if ("noRecordsMatch".equals(e.getCode())) {
                return new OaiPage(new ArrayList<>(), null);
            }
            throw new RuntimeException("OAI-PMH error: " + e.getCode() + " - " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("OAI-PMH request failed: " + e.getMessage(), e);
        }
    }

    private OaiRecord parseRecord(Record record) {
        try {
            Header header = record.getHeader();
            if (header == null) return null;

            Element metadata = record.getMetadata();
            if (metadata == null) {
                log.debug("[OAI-PMH] Record sin metadata: {}", header.getIdentifier());
                return null;
            }

            // Debug: mostrar nombres de elementos hijos
            if (log.isDebugEnabled()) {
                log.debug("[OAI-PMH] Metadata root: name='{}', ns='{}'", metadata.getName(), metadata.getNamespaceURI());
                for (Object child : metadata.elements()) {
                    if (child instanceof Element) {
                        Element el = (Element) child;
                        log.debug("  element name='{}', ns='{}'", el.getName(), el.getNamespaceURI());
                    }
                }
            }

            // metadata YA ES el elemento <oai_dc:dc>, usar directamente
            String title = getDcElementText(metadata, "title");
            String dcType = getDcElementText(metadata, "type");
            String dcDate = getDcElementText(metadata, "date");

            List<String> identifiers = getDcElementTexts(metadata, "identifier");

            String source = getDcElementText(metadata, "source");

            return new OaiRecord(
                header.getIdentifier(), header.getDatestamp(),
                title, dcType, dcDate, identifiers, source
            );

        } catch (Exception e) {
            log.warn("[OAI-PMH] Error parsing record: {}", e.getMessage());
            return null;
        }
    }

    private String getDcElementText(Element dc, String localName) {
        for (Object child : dc.elements()) {
            if (child instanceof Element) {
                Element el = (Element) child;
                String name = el.getName();
                // Manejar tanto "title" como "dc:title"
                if (name.equals(localName) || name.endsWith(":" + localName)) {
                    return el.getTextTrim();
                }
            }
        }
        return null;
    }

    private List<String> getDcElementTexts(Element dc, String localName) {
        List<String> values = new ArrayList<>();
        for (Object child : dc.elements()) {
            if (child instanceof Element) {
                Element el = (Element) child;
                String name = el.getName();
                // Manejar tanto "title" como "dc:title"
                if (name.equals(localName) || name.endsWith(":" + localName)) {
                    String text = el.getTextTrim();
                    if (!text.isEmpty()) {
                        values.add(text);
                    }
                }
            }
        }
        return values;
    }

    public record OaiRecord(
            String identifier,
            String datestamp,
            String title,
            String dcType,
            String dcDate,
            List<String> identifiers,
            String source
    ) {}

    public record OaiPage(
            List<OaiRecord> records,
            ResumptionToken resumptionToken
    ) {}
}
