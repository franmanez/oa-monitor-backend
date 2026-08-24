package es.oamonitor.seed;

import es.oamonitor.model.Institution;
import es.oamonitor.model.enums.OaiStatus;
import es.oamonitor.repository.InstitutionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RebiunSeed implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;

    public RebiunSeed(InstitutionRepository institutionRepository) {
        this.institutionRepository = institutionRepository;
    }

    @Override
    public void run(String... args) {
        if (institutionRepository.count() > 0) {
            return;
        }

        List<Institution> institutions = List.of(
            create("Universidad Complutense de Madrid", "https://ror.org/02p0gd045", "https://docta.ucm.es/oai/request", "oai_dc", "UCM"),
            create("Universidad de Barcelona", "https://ror.org/021018s57", "https://diposit.ub.edu/oai/request", "oai_dc", "UB"),
            create("Universidad Autónoma de Madrid", "https://ror.org/01cby8j38", "https://repositorio.uam.es/oai/request", "oai_dc", "UAM"),
            create("Universidad Autónoma de Barcelona", "https://ror.org/052g8jv94", "https://ddd.uab.cat/oai/request", "oai_dc", "UAB"),
            create("Universidad de Granada", "https://ror.org/04njjy449", "https://digibug.ugr.es/oai/request", "oai_dc", "UGR"),
            create("Universidad de Sevilla", "https://ror.org/03yxnpp24", "https://idus.us.es/oai/request", "oai_dc", "US"),
            create("Universidad de Valencia", "https://ror.org/043nxc105", "https://roderic.uv.es/oai/request", "oai_dc", "UV"),
            create("Universidad Politécnica de Madrid", "https://ror.org/03n6nwv02", "https://oa.upm.es/oai/request", "oai_dc", "UPM"),
            create("Universidad Politécnica de Cataluña", "https://ror.org/03mb6wj31", "https://upcommons.upc.edu/server/oai/revistes", "oai_dc", "UPC"),
            create("Universidad de Zaragoza", "https://ror.org/012a91z28", "https://zaguan.unizar.es/oai/request", "oai_dc", "UNIZAR"),
            create("Universidad del País Vasco", "https://ror.org/000xsnr85", "https://addi.ehu.eus/oai/request", "oai_dc", "UPV-EHU"),
            create("Universidad de Santiago de Compostela", "https://ror.org/030eybx10", "https://minerva.usc.es/oai/request", "oai_dc", "USC"),
            create("Universidad de Salamanca", "https://ror.org/02f40zc51", "https://gredos.usal.es/oai/request", "oai_dc", "USAL"),
            create("Universidad de Málaga", "https://ror.org/036b2ww28", "https://riuma.uma.es/oai/request", "oai_dc", "UMA"),
            create("Universidad de Murcia", "https://ror.org/03p3aeb86", "https://digitum.um.es/oai/request", "oai_dc", "UM"),
            create("Universidad de Oviedo", "https://ror.org/006g6ej02", "https://digibuo.uniovi.es/oai/request", "oai_dc", "UNIOVI"),
            create("Universidad de La Laguna", "https://ror.org/01rje8b92", "https://riull.ull.es/oai/request", "oai_dc", "ULL"),
            create("Universidad de Las Palmas de Gran Canaria", "https://ror.org/01eg5ch30", "https://accedacris.ulpgc.es/oai/request", "oai_dc", "ULPGC"),
            create("Universidad de Cantabria", "https://ror.org/046ffzj20", "https://repositorio.unican.es/oai/request", "oai_dc", "UNICAN"),
            create("Universidad de Castilla-La Mancha", "https://ror.org/05r7z1k40", "https://ruidera.uclm.es/oai/request", "oai_dc", "UCLM"),
            create("Universidad de Extremadura", "https://ror.org/0174shg90", "https://dehesa.unex.es/oai/request", "oai_dc", "UNEX"),
            create("Universidad de Jaén", "https://ror.org/019mrn953", "https://ruja.ujaen.es/oai/request", "oai_dc", "UJAEN"),
            create("Universidad de Cádiz", "https://ror.org/04skmn292", "https://rodin.uca.es/oai/request", "oai_dc", "UCA"),
            create("Universidad de Córdoba", "https://ror.org/05yc77b46", "https://helvia.uco.es/oai/request", "oai_dc", "UCO"),
            create("Universidad de Almería", "https://ror.org/003d3ww30", "https://repositorio.ual.es/oai/request", "oai_dc", "UAL"),
            create("Universidad de Huelva", "https://ror.org/03a1kt624", "https://rabida.uhu.es/oai/request", "oai_dc", "UHU"),
            create("Universidad Pablo de Olavide", "https://ror.org/01qnjqc44", "https://rio.upo.es/oai/request", "oai_dc", "UPO"),
            create("Universitat Jaume I", "https://ror.org/02ws1xc11", "https://repositori.uji.es/oai/request", "oai_dc", "UJI"),
            create("Universidad de Alicante", "https://ror.org/05t8bcz72", "https://rua.ua.es/oai/request", "oai_dc", "UA"),
            create("Universidad Miguel Hernández", "https://ror.org/02n0tza55", "https://dspace.umh.es/oai/request", "oai_dc", "UMH"),
            create("Universidad Politécnica de Valencia", "https://ror.org/01460j859", "https://riunet.upv.es/oai/request", "oai_dc", "UPV"),
            create("Universitat Pompeu Fabra", "https://ror.org/04n0g0b29", "https://repositori.upf.edu/oai/request", "oai_dc", "UPF"),
            create("Universitat de Lleida", "https://ror.org/050c3cw83", "https://repositori.udl.cat/oai/request", "oai_dc", "UDL"),
            create("Universitat de Girona", "https://ror.org/01xdxns91", "http://dugi-doc.udg.edu/dspace-oai/request", "oai_dc", "UDG"),
            create("Universitat Rovira i Virgili", "https://ror.org/010x8gc63", "https://repositori.urv.cat/oai/request", "oai_dc", "URV"),
            create("Universitat Oberta de Catalunya", "https://ror.org/03g49fa86", "https://openaccess.uoc.edu/oai/request", "oai_dc", "UOC"),
            create("Universitat de Vic - Universitat Central de Catalunya", "https://ror.org/03mdmxt95", "https://repositori.uvic.cat/oai/request", "oai_dc", "UVIC-UCC"),
            create("Universitat Ramon Llull", "https://ror.org/03akx8051", "https://recercat.cat/oai/request", "oai_dc", "URL"),
            create("Universidad de Navarra", "https://ror.org/02r6frx18", "https://dadun.unav.edu/oai/request", "oai_dc", "UNAV"),
            create("Universidad de Deusto", "https://ror.org/004gd9663", "https://dkh.deusto.es/oai/request", "oai_dc", "DEUSTO"),
            create("Universidad Pontificia Comillas", "https://ror.org/02hs7zw67", "https://repositorio.comillas.edu/oai/request", "oai_dc", "COMILLAS"),
            create("Universidad Carlos III de Madrid", "https://ror.org/03ths8210", "https://e-archivo.uc3m.es/oai/request", "oai_dc", "UC3M"),
            create("Universidad Rey Juan Carlos", "https://ror.org/01v5kym83", "https://burjcdigital.urjc.es/oai/request", "oai_dc", "URJC"),
            create("Universidad de Alcalá", "https://ror.org/04pmn0e78", "https://ebuah.uah.es/oai/request", "oai_dc", "UAH"),
            create("UNED - Universidad Nacional de Educación a Distancia", "https://ror.org/02msb5n36", "https://e-spacio.uned.es/oai/request", "oai_dc", "UNED"),
            create("Universidad de Burgos", "https://ror.org/027vm9h06", "https://riubu.ubu.es/oai/request", "oai_dc", "UBU"),
            create("Universidad de León", "https://ror.org/0122p5f64", "https://buleria.unileon.es/oai/request", "oai_dc", "UNILEON"),
            create("Universidad de Valladolid", "https://ror.org/041yk2d64", "https://uvadoc.uva.es/oai/request", "oai_dc", "UVA"),
            create("Universidade da Coruña", "https://ror.org/01qckj285", "https://ruc.udc.es/oai/request", "oai_dc", "UDC"),
            create("Universidade de Vigo", "https://ror.org/05rdf8595", "https://investigo.biblioteca.uvigo.es/oai/request", "oai_dc", "UVIGO"),
            create("Universidad de La Rioja", "https://ror.org/05rrfpt94", "https://investigacion.unirioja.es/oai/request", "oai_dc", "UNIRIOJA"),
            create("Universidad Pública de Navarra", "https://ror.org/01t0m9g58", "https://academica-e.unavarra.es/oai/request", "oai_dc", "UPNA"),
            create("Universitat de les Illes Balears", "https://ror.org/03e10x626", "https://dspace.uib.es/oai/request", "oai_dc", "UIB"),
            create("Universidad San Pablo CEU", "https://ror.org/04d1j1c02", "https://repositorioinstitucional.ceu.es/oai/request", "oai_dc", "CEU"),
            create("Universidad Francisco de Vitoria", "https://ror.org/052zeqq91", "https://ddfv.ufv.es/oai/request", "oai_dc", "UFV"),
            create("Universidad de Mondragón", "https://ror.org/04mdkpn78", "https://ebiltegia.mondragon.edu/oai/request", "oai_dc", "MONDRAGON"),
            create("Universitat Internacional de Catalunya", "https://ror.org/01tjv3f77", "https://recercat.cat/oai/request", "oai_dc", "UIC"),
            create("Universidad Católica de Valencia", "https://ror.org/02gza4b54", "https://riucv.ucv.es/oai/request", "oai_dc", "UCV"),
            create("Universidad Loyola Andalucía", "https://ror.org/01jc63w69", "https://repositorio.uloyola.es/oai/request", "oai_dc", "LOYOLA"),
            create("Universidad Camilo José Cela", "https://ror.org/02c911953", "https://repositorio.ucjc.edu/oai/request", "oai_dc", "UCJC"),
            create("Universidad Europea de Madrid", "https://ror.org/03z28j892", "https://abacus.universidadeuropea.com/oai/request", "oai_dc", "UEM"),
            create("Universidad Nebrija", "https://ror.org/02z7c0m60", "https://biblioteca.nebrija.es/oai/request", "oai_dc", "NEBRIJA"),
            create("Universidad Alfonso X El Sabio", "https://ror.org/02y91wx96", "https://repositorio.uax.es/oai/request", "oai_dc", "UAX"),
            create("IE University", "https://ror.org/04rr8gk74", "https://repositorio.ie.edu/oai/request", "oai_dc", "IE"),
            create("Universidad Internacional de La Rioja", "https://ror.org/03qsx4727", "https://reunir.unir.net/oai/request", "oai_dc", "UNIR"),
            create("Universidad Isabel I", "https://ror.org/0185x1x74", "https://repositorio.ui1.es/oai/request", "oai_dc", "UI1"),
            create("Universidad Politécnica de Cartagena", "https://ror.org/00jafnw98", "https://repositorio.upct.es/oai/request", "oai_dc", "UPCT"),
            create("Universidad Internacional Menéndez Pelayo", "https://ror.org/04qzzhg75", "https://repositorio.uimp.es/oai/request", "oai_dc", "UIMP"),
            create("Universidad Internacional de Andalucía", "https://ror.org/02k3x8184", "https://dspace.unia.es/oai/request", "oai_dc", "UNIA"),
            create("Universidad Cardenal Herrera CEU", "https://ror.org/01w0hpa91", "https://repositorioinstitucional.ceu.es/oai/request", "oai_dc", "UCH-CEU"),
            create("Universitat Abat Oliba CEU", "https://ror.org/013etmh24", "https://repositorioinstitucional.ceu.es/oai/request", "oai_dc", "UAO-CEU"),
            create("Universidad a Distancia de Madrid", "https://ror.org/05p3as182", "https://udimundus.udima.es/oai/request", "oai_dc", "UDIMA"),
            create("Universidad Católica de Ávila", "https://ror.org/05m69v858", "https://repositorio.ucavila.es/oai/request", "oai_dc", "UCAV"),
            create("Universidad Europea Miguel de Cervantes", "https://ror.org/04b8y0090", "https://repositorio.uemc.es/oai/request", "oai_dc", "UEMC")
        );

        institutionRepository.saveAll(institutions);
        System.out.println("Seed: " + institutions.size() + " institutions inserted");
    }

    private Institution create(String name, String rorId, String oaiEndpoint, String oaiMetadataPrefix, String rebiunCode) {
        Institution inst = new Institution();
        inst.setName(name);
        inst.setRorId(rorId);
        inst.setOaiEndpoint(oaiEndpoint);
        inst.setOaiMetadataPrefix(oaiMetadataPrefix);
        inst.setRebiunCode(rebiunCode);
        // Solo UPC y UDG activas en el seed; el resto se activa manualmente en BD
        if ("UPC".equals(rebiunCode) || "UDG".equals(rebiunCode)) {
            inst.setOaiStatus(OaiStatus.ACTIVE);
        } else {
            inst.setOaiStatus(OaiStatus.INACTIVE);
        }
        return inst;
    }
}
