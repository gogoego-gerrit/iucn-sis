package org.iucn.sis.server.utils.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.iucn.sis.server.io.AssessmentIO;
import org.iucn.sis.server.io.TaxaIO;
import org.iucn.sis.server.taxa.TaxonomyDocUtils;
import org.iucn.sis.server.users.utils.Arrays16Emulation;
import org.iucn.sis.server.utils.DocumentUtils;
import org.iucn.sis.server.utils.MostRecentFlagger;
import org.iucn.sis.server.utils.ServerPaths;
import org.iucn.sis.shared.BaseAssessment;
import org.iucn.sis.shared.data.assessments.AssessmentData;
import org.iucn.sis.shared.taxonomyTree.CommonNameData;
import org.iucn.sis.shared.taxonomyTree.TaxonNode;
import org.iucn.sis.shared.taxonomyTree.TaxonNodeFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.solertium.db.CanonicalColumnName;
import com.solertium.db.Column;
import com.solertium.db.DBException;
import com.solertium.db.DBSessionFactory;
import com.solertium.db.ExecutionContext;
import com.solertium.db.Row;
import com.solertium.db.SystemExecutionContext;
import com.solertium.db.query.QConstraint;
import com.solertium.db.query.SelectQuery;
import com.solertium.util.NodeCollection;
import com.solertium.vfs.ConflictException;
import com.solertium.vfs.NotFoundException;
import com.solertium.vfs.VFS;
import com.solertium.vfs.VFSPath;
import com.solertium.vfs.VFSPathToken;
import com.solertium.vfs.utils.VFSUtils;

public class TaxonModding {
	private static ExecutionContext ec;

	public static int changed = 0;
	public static StringBuilder log = new StringBuilder();

	private static List<String> footprint = null;

	private static int taxaCount = 0;

	public static int gmaSpeciesFound = 0;

	public static StringBuilder sameNamed = new StringBuilder();
	public static StringBuilder suspicious = new StringBuilder();

	public static int lcAmphibs = 0;
	public static int crAmphibs = 0;
	public static int newAmphibs = 0;
	
	private static String [] newLCAmphibs = new String [] { "Afrixalus aureus", "Bombina bombina", "Anaxyrus retiformis", "Rhinella atacamensis", "Heleioporus albopunctatus", "Hyla arborea", "Hypsiboas marianitae", "Lechriodus fletcheri", "Leptodactylus marambaiae", "Litoria dentata", "Litoria lesueurii", "Litoria verreauxii", "Mixophyes fasciolatus", "Pelobates fuscus", "Lithobates yavapaiensis", "Salamandra atra", "Tachycnemis seychellensis", "Triturus cristatus", "Eurycea spelaea", "Osteocephalus oophagus", "Hypsiboas andinus", "Boophis lichenoides", "Brachycephalus hermogenesi", "Onychodactylus fischeri", "Triturus karelinii", "Litoria genimaculata", "Litoria jervisiensis", "Litoria littlejohni", "Pseudophryne douglasi", "Pseudophryne semimarmorata", "Uperoleia aspera", "Uperoleia borealis", "Uperoleia crassa", "Uperoleia micromeles", "Uperoleia minima", "Uperoleia mjobergii", "Uperoleia talpa", "Uperoleia trachyderma", "Rhinella marina", "Litoria alboguttata", "Litoria australis", "Litoria brevipes", "Litoria cryptotis", "Litoria cultripes", "Litoria longipes", "Litoria maculosa", "Litoria maini", "Litoria manya", "Litoria novaehollandiae", "Litoria platycephala", "Litoria vagitus", "Litoria verrucosa", "Litoria adelaidensis", "Litoria bicolor", "Litoria burrowsi", "Litoria caerulea", "Litoria chloris", "Litoria citropa", "Litoria coplandi", "Litoria cyclorhyncha", "Litoria dahlii", "Litoria electrica", "Litoria eucnemis", "Litoria ewingii", "Litoria fallax", "Litoria gilleni", "Litoria gracilenta", "Litoria inermis", "Litoria infrafrenata", "Litoria latopalmata", "Litoria longirostris", "Litoria meiriana", "Litoria microbelos", "Litoria moorei", "Litoria nasuta", "Litoria nigrofrenata", "Litoria pallida", "Litoria paraewingi", "Litoria peronii", "Litoria personata", "Litoria phyllochroa", "Litoria revelata", "Litoria rothii", "Litoria rubella", "Litoria splendida", "Litoria tornieri", "Litoria tyleri", "Litoria watjulumensis", "Litoria xanthomera", "Cophixalus infacetus", "Cophixalus ornatus", "Arenophryne rotunda", "Assa darlingtoni", "Crinia bilingua", "Crinia deserticola", "Crinia georgiana", "Crinia glauerti", "Crinia insignifera", "Crinia parinsignifera", "Crinia pseudinsignifera", "Crinia remota", "Crinia riparia", "Crinia signifera", "Crinia subinsignifera", "Crinia tasmaniensis", "Geocrinia laevis", "Geocrinia leai", "Geocrinia rosea", "Geocrinia victoriana", "Heleioporus barycragus", "Heleioporus eyrei", "Heleioporus inornatus", "Heleioporus psammophilus", "Limnodynastes convexiusculus", "Limnodynastes depressus", "Limnodynastes dorsalis", "Limnodynastes dumerilii", "Limnodynastes fletcheri", "Limnodynastes interioris", "Platyplectrum ornatum", "Limnodynastes peronii", "Limnodynastes salmini", "Platyplectrum spenceri", "Limnodynastes tasmaniensis", "Limnodynastes terraereginae", "Metacrinia nichollsi", "Mixophyes schevilli", "Myobatrachus gouldii", "Neobatrachus albipes", "Neobatrachus aquilonius", "Neobatrachus centralis", "Neobatrachus fulvus", "Neobatrachus kunapalari", "Neobatrachus pelobatoides", "Neobatrachus pictus", "Neobatrachus sudelli", "Neobatrachus sutor", "Neobatrachus wilsmorei", "Notaden bennettii", "Notaden melanoscaphus", "Notaden nichollsi", "Paracrinia haswelli", "Pseudophryne coriacea", "Pseudophryne dendyi", "Pseudophryne guentheri", "Pseudophryne major", "Pseudophryne occidentalis", "Uperoleia altissima", "Uperoleia capitulata", "Uperoleia fusca", "Uperoleia glandulosa", "Uperoleia inundata", "Uperoleia laevigata", "Uperoleia lithomoda", "Uperoleia littlejohni", "Uperoleia mimula", "Uperoleia rugosa", "Uperoleia russelli", "Hylarana daemeli", "Limnonectes leytensis", "Limnonectes woodworthi", "Gastrotheca albolineata", "Limnonectes kohchangae", "Austrochaperina adelphe", "Austrochaperina fryi", "Austrochaperina gracilipes", "Austrochaperina pluvialis", "Austrochaperina robusta", "Crinia nimbus", 
		"Limnodynastes lignarius", "Ptychadena tellinii", "Amietia dracomontana", "Amietia vertebralis", "Arthroleptis adolfifriederici", "Arthroleptis affinis", "Arthroleptis lameerei", "Arthroleptis poecilonotus", "Arthroleptis schubotzi", "Arthroleptis stenodactylus", "Arthroleptis sylvaticus", "Arthroleptis taeniatus", "Arthroleptis variabilis", "Arthroleptis wahlbergii", "Arthroleptis xenodactyloides", "Cardioglossa elegans", "Cardioglossa escalerae", "Cardioglossa gracilis", "Cardioglossa gratiosa", "Cardioglossa leucomystax", "Arthroleptis xenochirus", "Ascaphus montanus", "Ascaphus truei", "Astylosternus batesi", "Astylosternus occidentalis", "Nyctibates corrugatus", "Scotobleps gabonicus", "Trichobatrachus robustus", "Bombina orientalis", "Bombina variegata", "Brachycephalus didactylus", "Brachycephalus ephippium", "Ansonia malayana", "Rhinella acutirostris", "Incilius alvarius", "Anaxyrus americanus", "Rhaebo anderssoni", "Bufo andrewsi", "Vandijkophrynus angusticeps", "Bufo arabicus", "Rhinella arenarum", "Rhinella arunco", "Amietophrynus asmarae", "Phrynoidis aspera", "Bufo atukoralei", "Bufo bankorensis", "Rhinella beebei", "Poyntonophrynus beiranus", "Rhinella bergi", "Ingerophrynus biporcatus", "Amietophrynus blanfordii", "Incilius bocourti", "Bufo bufo", "Epidalea calamita", "Amietophrynus camerunensis", "Incilius canaliferus", "Rhinella castaneotica", "Ingerophrynus celebensis", "Rhinella ceratophrys", "Incilius coccifer", "Anaxyrus cognatus", "Anaxyrus compactilis", "Incilius coniferus", "Nannophryne cophotis", "Rhinella crucifer", "Rhinella dapsilis", "Anaxyrus debilis", "Bufo dhufarensis", "Ingerophrynus divergens", "Bufo dodsoni", "Poyntonophrynus dombensis", "Rhinella dorbignyi", "Poyntonophrynus fenoulheti", "Rhinella fernandezae", "Rhinella fissipes", "Anaxyrus fowleri", "Amietophrynus fuliginatus", "Amietophrynus funereus", "Peltophryne fustiger", "Ingerophrynus galeatus", "Bufo gargarizans", "Vandijkophrynus gariepensis", "Amietophrynus garmani", "Rhaebo glaberrimus", "Amietophrynus gracilipes", "Rhinella granulosa", "Rhaebo guttatus", "Amietophrynus gutturalis", "Rhaebo haematiticus", "Anaxyrus hemiophrys", "Duttaphrynus himalayanus", "Poyntonophrynus hoeschi", "Rhinella icterica", "Rhinella inca", "Bufo japonicus", "Rhinella jimi", "Phrynoidis juxtaspera", "Amietophrynus kassasii", "Poyntonophrynus kavangensis", "Anaxyrus kelloggi", "Amietophrynus kerinyagae", "Amietophrynus kisoloensis", "Pseudepidalea latastii", "Amietophrynus latifrons", "Amietophrynus lemairii", "Rhinella limensis", "Mertensophryne lindneri", "Incilius luetkenii", "Poyntonophrynus lughensis", "Pseudepidalea luristanica", "Ingerophrynus macrotis", "Amietophrynus maculatus", "Rhinella margaritifera", "Incilius marmoreus", "Bufo mauritanicus", "Incilius mazatlanensis", "Incilius melanochlorus", "Mertensophryne melanopleura", "Duttaphrynus melanostictus", "Anaxyrus microscaphus", "Bufo minshanicus", "Rhaebo nasicus", "Incilius nebulifer", "Pseudepidalea oblonga", "Incilius occidentalis", "Rhinella ocellata", "Bufo olivaceus", "Amietophrynus pardalis", "Poyntonophrynus parkeri", "Ingerophrynus parvus", "Peltophryne peltocephala", "Bufo pentoni", "Ingerophrynus philippinicus", "Rhinella poeppigii", "Amietophrynus poweri", "Rhinella proboscidea", "Pseudepidalea pseudoraddei", "Anaxyrus punctatus", "Rhinella pygmaea", "Ingerophrynus quadriporcatus", "Anaxyrus quercicus", "Pseudepidalea raddei", "Amietophrynus rangeri", "Amietophrynus regularis",
		 "Vandijkophrynus robinsoni", "Rhinella roqueana", "Rhinella rubescens", "Bufo scaber", "Rhinella schneideri", "Anaxyrus speciosus", "Rhinella spinulosa", "Rhinella stanlaii", "Amietophrynus steindachneri", "Bufo stejnegeri", "Bufo stomaticus", "Amietophrynus superciliaris", "Pseudepidalea surda", "Mertensophryne taitana", "Anaxyrus terrestris", "Bufo tibetanus", "Bufo tihamicus", "Bufo torrenticola", "Amietophrynus tuberosus", "Incilius valliceps", "Nannophryne variegata", "Rhinella veraguensis", "Poyntonophrynus vertebralis", "Anaxyrus woodhousii", "Amietophrynus xeros", "Capensibufo tradouwi", "Crepidophryne epiotica", "Dendrophryniscus berthalutzae", "Dendrophryniscus bokermanni", "Dendrophryniscus brevipollicatus", "Dendrophryniscus leucomystax", "Dendrophryniscus minutus", "Frostius pernambucensis", "Leptophryne borbonica", "Melanophryniscus atroluteus", "Melanophryniscus fulvoguttatus", "Melanophryniscus klappenbachi", "Melanophryniscus rubriventris", "Melanophryniscus spectabilis", "Melanophryniscus stelzneri", "Melanophryniscus tumifrons", "Mertensophryne micranotis", "Nectophryne afra", "Nectophryne batesii", "Nectophrynoides tornieri", "Pedostibes hosii", "Pelophryne brevipes", "Pseudobufo subasper", "Schismaderma carens", "Mertensophryne loveridgei", "Centrolene andinum", "Centrolene grandisonae", "Centrolene hybrida", "Centrolene ilex", "Centrolene notostictum", "Centrolene prosoblepon", "Centrolene venezuelense", "Cochranella albomaculata", "Nymphargus bejaranoi", "Cochranella euknemos", "Cochranella flavopunctata", "Cochranella granulosa", "Cochranella midas", "Cochranella oyampiensis", "Cochranella resplendens", "Cochranella spinosa", "Hyalinobatrachium bergeri", "Hyalinobatrachium chirripoi", "Hyalinobatrachium colymbiphyllum", "Hyalinobatrachium crurifasciatum", "Hyalinobatrachium eurygnathum", "Hyalinobatrachium fleischmanni", "Hyalinobatrachium mondolfii", "Hyalinobatrachium munozorum", "Hyalinobatrachium nouraguense", "Cochranella pulverata", "Hyalinobatrachium ruedai", "Hyalinobatrachium talamancae", "Hyalinobatrachium taylori", "Hyalinobatrachium uranoscopum", "Hyalinobatrachium valerioi", "Allobates femoralis", "Allobates zaparo", "Hyloxalus abditaurantius", "Hyloxalus argyrogaster", "Hyloxalus bocagei", "Allobates brunneus", "Anomaloglossus degranvillei", "Silverstoneia flotator", "Colostethus inguinalis", "Allobates insperatus", "Hyloxalus littoralis", "Allobates marchesianus", "Hyloxalus nexipus", "Rheobates palmatus", "Colostethus panamansis", "Hyloxalus peruvianus", "Colostethus pratti", "Hyloxalus sauli", "Anomaloglossus stepheni", "Hyloxalus subpunctatus", "Allobates talamancae", "Allobates trilineatus", "Dendrobates auratus", "Ranitomeya biolat", "Adelphobates castaneoticus", "Ranitomeya duellmani", "Ranitomeya fantastica", "Ranitomeya fulgurita", "Adelphobates galactonotus", "Oophaga histrionica", "Ranitomeya imitator", "Ranitomeya lamasi", "Dendrobates leucomelas", "Ranitomeya minuta", "Oophaga pumilio", "Adelphobates quinquevittatus", "Ranitomeya reticulata", "Dendrobates tinctorius", "Dendrobates truncatus", "Ranitomeya vanzolinii", "Ranitomeya ventrimaculata", "Ameerega bilinguis", "Ameerega boliviana", "Epipedobates boulengeri", "Ameerega braccata", "Ameerega flavopicta", "Ameerega hahneli", "Ameerega macero", "Allobates myersi", "Ameerega parvula", "Ameerega petersi", "Ameerega picta", "Ameerega simulans", "Ameerega trivittata", "Phyllobates lugubris", "Alytes obstetricans", "Discoglossus galganoi", "Discoglossus pictus", "Discoglossus sardus", "Discoglossus scovazzi", "Heleophryne natalensis", "Heleophryne orientalis", "Heleophryne purcelli", "Heleophryne regis", "Hemisus guineensis", "Hemisus marmoratus", "Hemisus microscaphus", "Hemisus olivaceus", "Acris crepitans", "Acris gryllus", "Cruziohyla calcarifer", 
		 "Agalychnis callidryas", "Cruziohyla craspedopus", "Agalychnis saltator", "Agalychnis spurrelli", "Anotheca spinosa", "Aparasphenodon brunoi", "Aparasphenodon venezolanus", "Aplastodiscus cochranae", "Aplastodiscus perviridis", "Corythomantis greeningi", "Duellmanohyla rufioculis", "Flectonotus fissilis", "Flectonotus goeldii", "Flectonotus ohausi", "Flectonotus pygmaeus", "Gastrotheca argenteovirens", "Gastrotheca dunni", "Gastrotheca fissipes", "Gastrotheca griswoldi", "Gastrotheca longipes", "Gastrotheca marsupiata", "Gastrotheca microdiscus", "Gastrotheca monticola", "Gastrotheca nicefori", "Gastrotheca peruana", "Gastrotheca testudinea", "Hemiphractus helioi", "Hemiphractus proboscideus", "Hemiphractus scutatus", "Dendropsophus acreanus", "Aplastodiscus albofrenatus", "Osteocephalus alboguttatus", "Hypsiboas albomarginatus", "Hypsiboas albopunctatus", "Hyloscirtus albopunctulatus", "Aplastodiscus albosignatus", "Dendropsophus allenorum", "Bokermannohyla alvarengai", "Dendropsophus anataliasiasi", "Dendropsophus anceps", "Hyla annectans", "Dendropsophus aperomeus", "Hyla arenicolor", "Aplastodiscus arildae", "Hyloscirtus armatus", "Bokermannohyla astartea", "Hypsiboas atlanticus", "Hyla avivoca", "Dendropsophus baileyi", "Hypsiboas balzani", "Hypsiboas benitezi", "Dendropsophus berthalutzae", "Dendropsophus bifurcus", "Hypsiboas lundii", "Dendropsophus bipunctatus", "Hypsiboas bischoffi", "Plectrohyla bistincta", "Hypsiboas boans", "Dendropsophus bogerti", "Dendropsophus bokermanni", "Dendropsophus branneri", "Dendropsophus brevifrons", "Hypsiboas caingua", "Hypsiboas calcaratus", "Hypsiboas callipleura", "Aplastodiscus callipygius", "Dendropsophus carnifex", "Bokermannohyla carvalhoi", "Hyla chinensis", "Hyla chrysoscelis", "Hyla cinerea", "Bokermannohyla circumdata", "Dendropsophus columbianus", "Hypsiboas crepitans", "Dendropsophus cruzi", "Dendropsophus decipiens", "Dendropsophus delarivai", "Hypsiboas dentei", "Dendropsophus ebraccatus", "Aplastodiscus ehrhardti", "Dendropsophus elegans", "Dendropsophus elianeae", "Hyla eximia", "Hypsiboas faber", "Hypsiboas fasciatus", "Hyla femoralis", "Dendropsophus garagoensis", "Dendropsophus gaucheri", "Hypsiboas geographicus", "Dendropsophus giesleri", "Hypsiboas goianus", "Hypsiboas cinerascens", "Hyla gratiosa", "Hypsiboas guentheri", "Dendropsophus haddadi", "Hyla hallowellii", "Dendropsophus haraldschultzi", "Hypsiboas hobbsi", "Hypsiboas hutchinsi", "Bokermannohyla hylax", "Aplastodiscus ibirapitanga", "Hyla immaculata", "Hyla intermedia", "Hyla japonica", "Dendropsophus jimi", "Hypsiboas joaquini", "Myersiohyla kanaima", "Scinax karenanneae", "Dendropsophus koechlini", "Dendropsophus labialis", "Isthmohyla lancasteri", "Hypsiboas lanciformis", "Hyloscirtus lascinius", "Dendropsophus leali", "Hypsiboas lemai", "Hypsiboas leptolineatus", "Dendropsophus leucophyllatus", "Aplastodiscus leucopygius", "Tlalocohyla loquax", "Bokermannohyla luctuosa", "Dendropsophus luteoocellatus", "Hypsiboas marginatus", "Dendropsophus marmoratus", "Bokermannohyla martinsi", "Dendropsophus mathiassoni", "Dendropsophus melanargyreus", "Dendropsophus meridianus", "Hyla meridionalis", "Dendropsophus microcephalus", "Hypsiboas microderma", "Dendropsophus microps", "Dendropsophus minusculus", "Dendropsophus minutus", "Dendropsophus miyatai", "Hypsiboas multifasciatus", "Dendropsophus nahdereri", "Dendropsophus nanus", "Bokermannohyla nanuzae", "Dendropsophus oliveirai", "Hypsiboas ornatissimus",
		"Dendropsophus padreluna", "Hyloscirtus palmeri", "Hypsiboas pardalis", "Dendropsophus parviceps", "Dendropsophus pauiniensis", "Dendropsophus pelidna", "Hypsiboas pellucens", "Dendropsophus phlebodes", "Hyloscirtus phyllognathus", "Tlalocohyla picta", "Hypsiboas picturatus", "Hyla plicata", "Hypsiboas polytaenius", "Dendropsophus praestans", "Hypsiboas prasinus", "Dendropsophus pseudomeridianus", "Bokermannohyla pseudopseudis", "Isthmohyla pseudopuma", "Hypsiboas pugnax", "Hypsiboas pulchellus", "Hypsiboas punctatus", "Hypsiboas raniceps", "Dendropsophus rhodopeplus", "Dendropsophus riveroi", "Dendropsophus robertmertensi", "Hypsiboas rosenbergi", "Dendropsophus rossalleni", "Dendropsophus rubicundulus", "Hypsiboas rubracylus", "Hypsiboas rufitelus", "Dendropsophus sanborni", "Hyla sanchiangensis", "Dendropsophus sarayacuensis", "Hyla sarda", "Dendropsophus sartori", "Hyla savignyi", "Bokermannohyla saxicola", "Dendropsophus schubarti", "Hypsiboas semiguttatus", "Hypsiboas semilineatus", "Dendropsophus seniculus", "Hypsiboas sibleszi", "Hyla simplex", "Exerodonta smaragdina", "Tlalocohyla smithii", "Dendropsophus soaresi", "Hyla squirella", "Dendropsophus subocularis", "Exerodonta sumichrasti", "Dendropsophus timbeba", "Dendropsophus triangulum", "Dendropsophus tritaeniatus", "Hyla tsinlingensis", "Ecnomiohyla tuberculosa", "Scinax uruguayus", "Hyla versicolor", "Scarthyla vigilans", "Dendropsophus virolinensis", "Dendropsophus walfordi", "Hypsiboas wavrini", "Dendropsophus werneri", "Hyla wrightorum", "Dendropsophus xapuriensis", "Hylomantis aspera", "Hylomantis granulosa", "Litoria amboinensis", "Litoria angiana", "Litoria arfakiana", "Litoria congenita", "Litoria darlingtoni", "Litoria dorsalis", "Litoria exophthalmia", "Litoria havina", "Litoria impura", "Litoria iris", "Litoria louisiadensis", "Litoria micromembrana", "Litoria modica", "Litoria multiplica", "Litoria napaea", "Litoria nigropunctata", "Litoria pronimia", "Litoria prora", "Litoria pygmaea", "Litoria spinifera", "Litoria thesaurensis", "Litoria timida", "Litoria vocivincens", "Litoria wollastoni", "Pseudis caraya", "Pseudis laevis", "Pseudis limellum", "Nyctimantis rugiceps", "Litoria cheesmani", "Litoria disrupta", "Litoria foricula", "Litoria humeralis", "Litoria kubori", "Litoria narinosa", "Litoria perimetri", "Litoria pulchra", "Litoria semipalmata", "Litoria trachydermis", "Osteocephalus buckleyi", "Osteocephalus cabrerai", "Osteocephalus deridens", "Osteocephalus elkejungingerae", "Osteocephalus heyeri", "Itapotihyla langsdorffii", "Osteocephalus leoniae", "Osteocephalus leprieurii", "Osteocephalus mutabor", "Osteocephalus pearsoni", "Osteocephalus planiceps", "Osteocephalus subtilis", "Osteocephalus taurinus", "Osteocephalus verruciger", "Osteocephalus yasuni", "Osteopilus brunneus", "Osteopilus dominicensis", "Osteopilus septentrionalis", "Pachymedusa dacnicolor", "Phasmahyla cochranae", "Phasmahyla exilis", "Phasmahyla guttata", "Phasmahyla jandaia", "Trachycephalus coriaceus", "Trachycephalus hadroceps", "Trachycephalus imitatrix", "Trachycephalus mesophaeus", "Trachycephalus resinifictrix", "Trachycephalus venulosus", "Phrynomedusa marginata", "Phyllodytes acuminatus", "Phyllodytes kautskyi", "Phyllodytes luteolus", "Phyllodytes melanomystax", "Phyllomedusa atelopoides", "Phyllomedusa bicolor", "Phyllomedusa boliviana", "Hylomantis buckleyi", "Phyllomedusa burmeisteri", "Phyllomedusa camba", "Phyllomedusa coelestis", "Phyllomedusa distincta", "Hylomantis hulli", 
		"Phyllomedusa hypochondrialis", "Phyllomedusa iheringii", "Phyllomedusa palliata", "Phyllomedusa rohdei", "Phyllomedusa sauvagii", "Phyllomedusa tarsius", 
		"Phyllomedusa tetraploidea", "Phyllomedusa tomopterna", "Phyllomedusa trinitatis", "Phyllomedusa vaillantii", "Phyllomedusa venusta", "Pseudacris brachyphona", "Pseudacris brimleyi", "Pseudacris cadaverina", "Pseudacris clarkii", "Pseudacris crucifer", "Pseudacris feriarum", "Pseudacris nigrita", "Pseudacris ocularis", "Pseudacris ornata", "Pseudacris regilla", "Pseudacris streckeri", "Pseudacris triseriata", "Pseudis bolbodactyla", "Pseudis cardosoi", "Pseudis fusca", "Pseudis minuta", "Pseudis paradoxa", "Pseudis tocantins", "Smilisca fodiens", "Scarthyla goinorum", "Scinax acuminatus", "Scinax agilis", "Scinax albicans", "Scinax altae", "Scinax alter", "Scinax angrensis", "Scinax argyreornatus", "Scinax auratus", "Scinax berthae", "Scinax blairi", "Scinax boesemani", "Scinax boulengeri", "Scinax brieni", "Scinax caldarum", "Scinax cardosoi", "Scinax carnevallii", "Scinax catharinae", "Scinax centralis", "Scinax chiquitanus", "Scinax crospedospilus", "Scinax cruentommus", "Scinax cuspidatus", "Scinax duartei", "Scinax elaeochrous", "Scinax eurydice", "Scinax exiguus", "Scinax flavoguttatus", "Scinax funereus", "Scinax fuscomarginatus", "Scinax fuscovarius", "Scinax garbei", "Scinax granulatus", "Scinax hayii", "Scinax hiemalis", "Scinax humilis", "Scinax ictericus", "Scinax kennedyi", "Scinax lindsayi", "Scinax littoralis", "Scinax littoreus", "Scinax longilineus", "Scinax luizotavioi", "Scinax machadoi", "Scinax flavidus", "Scinax nasicus", "Scinax nebulosus", "Scinax obtriangulatus", "Scinax pachycrus", "Scinax parkeri", "Scinax pedromedinae", "Scinax perereca", "Scinax perpusillus", "Scinax proboscideus", "Scinax quinquefasciatus", "Scinax rizibilis", "Scinax rostratus", "Scinax ruber", "Scinax similis", "Scinax squalirostris", "Scinax staufferi", "Scinax sugillatus", "Scinax trilineatus", "Scinax v-signatus", "Scinax wandae", "Scinax x-signatus", "Smilisca baudinii", "Smilisca phaeota", "Smilisca puma", "Smilisca sila", "Smilisca sordida", "Sphaenorhynchus carneus", "Sphaenorhynchus dorisae", "Sphaenorhynchus lacteus", "Sphaenorhynchus orophilus", "Sphaenorhynchus palustris", "Sphaenorhynchus planicola", "Sphaenorhynchus prasinus", "Sphaenorhynchus surdus", "Stefania evansi", "Stefania ginesi", "Stefania scalae", "Stefania woodleyi", "Tepuihyla edelcae", "Trachycephalus atlas", "Trachycephalus jordani", "Trachycephalus nigromaculatus", "Triprion petasatus", "Diaglena spatulata", "Acanthixalus spinosus", "Afrixalus brachycnemis", "Afrixalus crotalus", "Afrixalus delicatus", "Afrixalus dorsalis", "Afrixalus equatorialis", "Afrixalus fornasini", "Afrixalus fulvovittatus", "Afrixalus laevis", "Afrixalus leucostictus", "Afrixalus osorioi", "Afrixalus paradorsalis", "Afrixalus quadrivittatus", "Afrixalus septentrionalis", "Afrixalus stuhlmanni", "Afrixalus vittiger", "Afrixalus weidholzi", "Afrixalus wittei", "Alexteroon hypsiphonus", "Alexteroon obstetricans", "Chlorolius koehleri", "Cryptothylax greshoffii", "Heterixalus alboguttatus", "Heterixalus andrakata", "Heterixalus betsileo", "Heterixalus boettgeri", "Heterixalus luteostriatus", "Heterixalus madagascariensis", "Heterixalus punctatus", "Heterixalus tricolor", "Heterixalus variabilis", "Hyperolius acuticeps", "Hyperolius argus", "Hyperolius balfouri", "Hyperolius baumanni", "Hyperolius benguellensis", "Hyperolius bolifambae", "Hyperolius cinnamomeoventris", "Hyperolius concolor", "Hyperolius fusciventris", "Hyperolius glandicolor", "Hyperolius guttulatus", "Hyperolius kachalolae", "Hyperolius kivuensis", "Hyperolius kuligae", "Hyperolius lamottei", "Hyperolius langi", "Hyperolius lateralis", "Hyperolius major", "Hyperolius marginatus", "Hyperolius mariae",
		 "Hyperolius marmoratus", "Hyperolius mitchelli", "Hyperolius montanus", "Hyperolius mosaicus", "Hyperolius nasutus", "Hyperolius nitidulus", "Hyperolius occidentalis", "Hyperolius ocellatus", "Hyperolius parallelus", "Hyperolius pardalis", "Hyperolius parkeri", "Hyperolius phantasticus", "Hyperolius picturatus", "Hyperolius pictus", "Hyperolius platyceps", "Hyperolius pseudargus", "Hyperolius pusillus", "Hyperolius pyrrhodictyon", "Hyperolius quinquevittatus", "Hyperolius reesi", "Hyperolius rhodesianus", "Hyperolius schoutedeni", "Hyperolius semidiscus", "Hyperolius sheldricki", "Hyperolius spinigularis", "Hyperolius steindachneri", "Hyperolius swynnertoni", "Hyperolius sylvaticus", "Hyperolius tuberculatus", "Hyperolius tuberilinguis", "Hyperolius viridiflavus", "Kassina cassinoides", "Kassina fusca", "Kassina kuvangensis", "Kassina maculata", "Kassina maculifer", "Kassina maculosa", "Kassina schioetzi", "Kassina senegalensis", "Kassina somalica", "Kassinula wittei", "Leptopelis anchietae", "Leptopelis argenteus", "Leptopelis aubryi", "Leptopelis bocagii", "Leptopelis boulengeri", "Leptopelis brevirostris", "Leptopelis broadleyi", "Leptopelis bufonides", "Leptopelis calcaratus", "Leptopelis christyi", "Leptopelis concolor", "Leptopelis cynnamomeus", "Leptopelis flavomaculatus", "Leptopelis gramineus", "Leptopelis spiritusnoctis", "Leptopelis millsoni", "Leptopelis modestus", "Leptopelis mossambicus", "Leptopelis natalensis", "Leptopelis nordequatorialis", "Leptopelis notatus", "Leptopelis ocellatus", "Leptopelis omissus", "Leptopelis oryi", "Leptopelis parbocagii", "Leptopelis rufus", "Leptopelis viridis", "Hyperolius molleri", "Opisthothylax immaculatus", "Paracassina kounhiensis", "Paracassina obscura", "Phlyctimantis boulengeri", "Phlyctimantis leonardi", "Phlyctimantis verrucosus", "Semnodactylus wealii", "Adelophryne adiastola", "Adelophryne gutturosa", "Leptodactylus andreae", "Leptodactylus araucaria", "Leptodactylus bokermanni", "Leptodactylus diptyx", "Leptodactylus heyeri", "Leptodactylus hylaedactylus", "Leptodactylus marmoratus", "Leptodactylus martinezi", "Alsodes gargola", "Barycholos pulcher", "Barycholos ternetzi", "Batrachyla antartandica", "Batrachyla leptopus", "Batrachyla taeniata", "Ceratophrys aurita", "Ceratophrys calcarata", "Ceratophrys cornuta", "Ceratophrys cranwelli", "Chacophrys pierottii", "Crossodactylus caramaschii", "Crossodactylus gaudichaudii", "Cycloramphus boraceiensis", "Cycloramphus dubius", "Cycloramphus fuliginosus", "Cycloramphus rhyakonastes", "Edalorhina perezi", "Pristimantis aaptus", "Eleutherodactylus abbotti", "Pristimantis acatallelus", "Pristimantis achatinus", "Pristimantis acuminatus", "Pristimantis altamazonicus", "Strabomantis anomalus", "Eleutherodactylus antillensis", "Pristimantis appendiculatus", "Eleutherodactylus atkinsi", "Craugastor augusti", "Eleutherodactylus auriculatus", "Hypodactylus babax", "Ischnocnema bilineata", "Haddadus binotatus", "Pristimantis bogotensis", "Ischnocnema bolbodactyla", "Pristimantis boulengeri", "Craugastor bransfordii", "Pristimantis brevifrons", "Eleutherodactylus brittoni", "Pristimantis buccinator", "Pristimantis buckleyi", "Strabomantis bufoniformis", "Pristimantis cajamarcensis", "Pristimantis caprifer", "Pristimantis carvalhoi", "Pristimantis cerasinus", "Strabomantis cerastes", "Pristimantis chalceus", "Pristimantis chiastonotus", "Pristimantis chloronotus", "Eleutherodactylus cochranae", "Pristimantis conspicillatus", "Eleutherodactylus coqui", "Craugastor crassidigitus", "Pristimantis croceoinguinis", "Pristimantis cruentus", "Oreobates cruralis", "Eleutherodactylus cuneatus", "Pristimantis curtipes", 
		 "Eleutherodactylus cystignathoides", "Pristimantis danae", "Pristimantis diadematus", "Diasporus diastema", "Oreobates discoidalis", "Pristimantis erythropleura", "Pristimantis eurydactylus", "Pristimantis factiosus", "Pristimantis fenestratus", "Craugastor fitzingeri", "Pristimantis fraudator", "Pristimantis gaigei", "Craugastor gollmeri", "Eleutherodactylus gossei", "Ischnocnema gualteri", "Ischnocnema guentheri", "Diasporus gularis", "Eleutherodactylus guttilatus", "Pristimantis gutturalis", "Ischnocnema hoehnei", "Diasporus hylaeformis", "Oreobates ibischi", "Pristimantis imitatrix", "Pristimantis inguinalis", "Eleutherodactylus inoptatus", "Eleutherodactylus johnstonei", "Ischnocnema juipoca", "Pristimantis labiosus", "Pristimantis lacrimosus", "Ischnocnema lactea", "Pristimantis lanthanites", "Pristimantis latidiscus", "Pristimantis leoni", "Pristimantis leptolophus", "Pristimantis llojsintuta", "Craugastor loki", "Craugastor longirostris", "Pristimantis lymani", "Pristimantis lythrodes", "Pristimantis malkini", "Hypodactylus mantipus", "Pristimantis marmoratus", "Eleutherodactylus marnockii", "Pristimantis martiae", "Pristimantis medemi", "Craugastor megacephalus", "Craugastor melanostictus", "Pristimantis mendax", "Craugastor mexicanus", "Craugastor mimus", "Pristimantis moro", "Pristimantis myersi", "Ischnocnema nasuta", "Pristimantis nervicus", "Pristimantis nicefori", "Hypodactylus nigrovittatus", "Eleutherodactylus nitidus", "Craugastor noblei", "Pristimantis obmutescens", "Pristimantis ockendeni", "Ischnocnema octavioi", "Craugastor opimus", "Pristimantis orcesi", "Pristimantis paisa", "Pristimantis palmeri", "Pristimantis parvillus", "Ischnocnema parva", "Ischnocnema paulodutrai", "Pristimantis paululus", "Pristimantis peraticus", "Pristimantis permixtus", "Pristimantis peruvianus", "Pristimantis phoxocephalus", "Pristimantis piceus", "Eleutherodactylus pipilans", "Eleutherodactylus planirostris", "Pristimantis platydactylus",
		"Pristimantis pluvicanorus", "Craugastor polyptychus", "Pristimantis prolixodiscus", "Pristimantis pseudoacuminatus", "Pristimantis pulvinatus", "Pristimantis quaquaversus", "Diasporus quidditus", "Pristimantis racemus", "Ischnocnema ramagii", "Craugastor raniformis", "Pristimantis restrepoi", "Pristimantis rhabdolaemus", "Pristimantis ridens", "Eleutherodactylus riparius", "Craugastor rugosus", "Craugastor rugulosus", "Craugastor rupinius", "Pristimantis samaipatae", "Pristimantis skydmainos", "Craugastor stejnegerianus", "Pristimantis subsigillatus", "Strabomantis sulcatus", "Pristimantis taeniatus", "Craugastor talamancae", "Pristimantis terraebolivaris", "Pristimantis thectopternus", "Pristimantis thymelensis", "Diasporus tinker", "Pristimantis toftae", "Craugastor underwoodi", "Pristimantis unistrigatus", "Pristimantis uranobates", "Pristimantis variabilis", "Eleutherodactylus varleyi", "Ischnocnema venancioi", "Pristimantis ventrimarmoratus", "Pristimantis viejas", "Pristimantis vilarsi", "Ischnocnema vinhai", "Craugastor vocalis", "Diasporus vocator", "Pristimantis walkeri", "Eleutherodactylus weinlandi", "Pristimantis w-nigrum", "Pristimantis zeuctotylus", "Pristimantis zimmermanae", "Strabomantis zygodactylus", "Euparkerella brasiliensis", "Euparkerella cochranae", "Eupsophus calcaratus", "Eupsophus emiliopugini", "Hydrolaetare dantasi", "Hydrolaetare schmidti", "Hylodes asper", "Hylodes lateristrigatus", "Hylodes meridionalis", "Hylodes nasus", "Hylodes ornatus", "Hylodes perplicatus", "Hylodes phyllodes", "Hylorina sylvatica", "Oreobates quixensis", "Oreobates sanctaecrucis", "Lepidobatrachus laevis", "Lepidobatrachus llanensis", "Leptodactylus albilabris", "Leptodactylus bolivianus", "Leptodactylus bufonius", "Leptodactylus caatingae", "Leptodactylus chaquensis", "Leptodactylus colombiensis", "Leptodactylus cunicularius", "Leptodactylus didymus", "Leptodactylus diedrus", "Leptodactylus elenae", "Leptodactylus flavopictus", "Leptodactylus fragilis", "Leptodactylus furnarius", "Leptodactylus fuscus", "Leptodactylus gracilis", "Leptodactylus griseigularis", "Leptodactylus knudseni", "Leptodactylus labrosus", "Leptodactylus labyrinthicus", "Leptodactylus latinasus", "Leptodactylus leptodactyloides", "Leptodactylus lithonaetes", "Leptodactylus longirostris", "Leptodactylus melanonotus", "Leptodactylus myersi", "Leptodactylus mystaceus", "Leptodactylus mystacinus", "Leptodactylus natalensis", "Leptodactylus notoaktites", "Leptodactylus ocellatus", "Leptodactylus pallidirostris", "Leptodactylus pentadactylus", "Leptodactylus petersii", "Leptodactylus plaumanni", "Leptodactylus podicipinus", "Leptodactylus poecilochilus", "Leptodactylus pustulatus", "Leptodactylus rhodomystax", "Leptodactylus rhodonotus", "Leptodactylus riveroi", "Leptodactylus rugosus", "Leptodactylus sabanensis", "Leptodactylus spixi", "Leptodactylus stenodema", "Leptodactylus syphax", "Leptodactylus troglodytes", "Leptodactylus validus", "Leptodactylus ventrimaculatus", "Leptodactylus wagneri", "Limnomedusa macroglossa", "Leptodactylus lineatus", "Macrogenioglottus alipioi", "Megaelosia goeldii", "Odontophrynus americanus", "Odontophrynus carvalhoi", "Odontophrynus cordobae", "Odontophrynus cultripes", "Odontophrynus lavillai", "Odontophrynus occidentalis", "Noblella carrascoicola", "Noblella myrmecoides", "Physalaemus aguirrei", "Physalaemus albifrons", "Physalaemus albonotatus", "Physalaemus biligonigerus", "Physalaemus centralis", "Physalaemus cicada", "Physalaemus crombiei", "Physalaemus cuqui", "Physalaemus cuvieri", "Physalaemus ephippifer", "Physalaemus fernandezae", "Physalaemus fischeri", 
		"Physalaemus marmoratus", "Physalaemus gracilis", "Physalaemus henselii", "Physalaemus kroyeri", "Physalaemus lisei", "Physalaemus maculiventris", "Engystomops montubio", "Physalaemus nanus", "Eupemphix nattereri", "Physalaemus olfersii", "Physalaemus petersi", "Engystomops pustulatus", "Engystomops pustulosus", "Engystomops randi", "Physalaemus riograndensis", "Physalaemus santafecinus", "Physalaemus signifer", "Physalaemus spiniger", "Phyzelaphryne miriamae", "Pleurodema borellii", "Pleurodema brachyops", "Pleurodema bufoninum", "Pleurodema cinereum", "Pleurodema diplolister", "Pleurodema guayapae", "Pleurodema marmoratum", "Pleurodema nebulosum", "Pleurodema thaul", "Pleurodema tucumanum", "Proceratophrys appendiculata", "Proceratophrys avelinoi", "Proceratophrys boiei", "Proceratophrys brauni", "Proceratophrys cristiceps", "Proceratophrys fryi", "Proceratophrys goyana", "Proceratophrys laticeps", "Proceratophrys melanopogon", "Proceratophrys schirchi", "Proceratophrys subguttata", "Pseudopaludicola boliviana", "Pseudopaludicola ceratophryes", "Pseudopaludicola falcipes", "Pseudopaludicola llanera", "Pseudopaludicola mystacalis", "Pseudopaludicola pusilla", "Pseudopaludicola saltica", "Pseudopaludicola ternetzi", "Scythrophrys sawayae", "Telmatobius rimac", "Thoropa megatympanum", "Thoropa miliaris", "Leptodactylus discodactylus", "Zachaenus parvulus", "Lechriodus aganoposis", "Lechriodus melanopyga", "Lechriodus platyceps", "Aglyptodactylus madagascariensis", "Aglyptodactylus securifer", "Boophis albilabris", "Boophis albipunctatus", "Boophis ankaratra", "Boophis boehmei", "Boophis bottae", "Boophis doulioti", "Boophis erythrodactylus", "Boophis goudotii", "Boophis guibei", "Boophis idae", "Boophis luteus", "Boophis madagascariensis", "Boophis marojezensis", "Boophis microtympanum", "Boophis miniatus", "Boophis opisthodon", "Boophis pauliani", "Boophis picturatus", "Boophis pyrrhus", "Boophis rappiodes", "Boophis reticulatus", "Boophis tasymena", "Boophis tephraeomystax", "Boophis viridis", "Boophis vittatus", "Laliostoma labrosum", "Mantella baroni", "Mantella betsileo", "Mantella nigricans", "Mantidactylus aerumnalis", "Spinomantis aglavei", "Mantidactylus alutus", "Mantidactylus ambreensis", "Mantidactylus argenteus", "Gephyromantis asper", "Mantidactylus betsileanus", "Guibemantis bicalcaratus", "Mantidactylus biporus", "Blommersia blommersae", "Gephyromantis boulengeri", "Mantidactylus brevipalmatus", "Mantidactylus charlotteae", "Mantidactylus curtus", "Guibemantis depressiceps", "Blommersia domerguei", "Mantidactylus femoralis", "Spinomantis fimbriatus", "Guibemantis flavobrunneus", "Mantidactylus grandidieri", "Blommersia grandisonae", "Gephyromantis granulatus", "Mantidactylus guttulatus", "Blommersia kely", "Guibemantis liber", "Mantidactylus lugubris", "Gephyromantis luteus", "Mantidactylus majori", "Gephyromantis malagasius", "Mantidactylus melanopleura", "Mantidactylus mocquardi", "Gephyromantis moseri", "Mantidactylus opiparis", "Spinomantis peraccae", "Spinomantis phantasticus", "Gephyromantis pseudoasper", "Guibemantis pulcher", "Gephyromantis redimitus", "Gephyromantis sculpturatus", "Guibemantis tornieri", "Mantidactylus ulcerosus", "Gephyromantis ventrimaculatus", "Blommersia wittei", "Mantidactylus zipperi", "Brachytarsophrys carinense", "Brachytarsophrys feae", "Brachytarsophrys platyparietus", "Leptobrachella mjobergi", "Leptobrachium abbotti", "Leptobrachium chapaense", "Leptobrachium hasseltii", "Leptobrachium hendricksoni", "Leptobrachium montanum", "Leptobrachium nigrops", "Leptobrachium smithi", "Leptolalax heteropus", "Leptolalax liui", "Leptolalax oshanensis", 
		"Leptolalax pelodytoides", "Megophrys montana", "Megophrys nasuta", "Ophryophryne microstoma", "Ophryophryne pachyproctus", "Oreolalax popei", "Oreolalax xiangchengensis", "Scutiger boulengeri", "Scutiger glandulatus", "Scutiger mammatus", "Scutiger nyingchiensis", "Scutiger sikimmensis", "Leptobrachium liui", "Xenophrys aceras", "Xenophrys boettgeri", "Xenophrys glandulosa", "Xenophrys jingdongensis", "Xenophrys kuatunensis", "Xenophrys major", "Xenophrys minor", "Xenophrys palpebralespinosa", "Xenophrys parva", "Xenophrys shapingensis", "Xenophrys spinata", "Xenophrys wushanensis", "Albericus brunhildae", "Albericus darlingtoni", "Albericus swanhildae", "Albericus tuberculus", "Albericus valkuriarum", "Anodonthyla boulengerii", "Aphantophryne pansa", "Arcovomer passarellii", "Asterophrys turpicola", "Austrochaperina basipalmata", "Austrochaperina blumi", "Austrochaperina derongo", "Austrochaperina guttata", "Austrochaperina hooglandi", "Austrochaperina macrorhyncha", "Austrochaperina palmipes", "Austrochaperina rivularis", "Barygenys atra", "Barygenys exsul", "Barygenys nana", "Breviceps acutirostris", "Breviceps adspersus", "Breviceps fuscus", "Breviceps montanus", "Breviceps mossambicus", "Breviceps namaquensis", "Breviceps poweri", "Breviceps rosei", "Breviceps verrucosus", "Calluella guttulata", "Calluella yunnanensis", "Callulina kreffti", "Callulops comptus", "Callulops doriae", "Callulops humicola", "Callulops personatus", "Callulops robustus", "Callulops slateri", "Callulops stictogaster", "Callulops wilhelmanus", "Chaperina fusca", "Chiasmocleis albopunctata", "Chiasmocleis anatipes", "Chiasmocleis atlantica", "Chiasmocleis bassleri", "Chiasmocleis capixaba", "Chiasmocleis hudsoni", "Chiasmocleis leucosticta", "Chiasmocleis panamensis", "Chiasmocleis schubarti", "Chiasmocleis shudikarensis", "Chiasmocleis ventrimaculata", "Choerophryne proboscidea", "Choerophryne rostellifer", "Cophixalus biroi", "Cophixalus cheesmanae", "Cophixalus parkeri", "Cophixalus pipilans", "Cophixalus riparius", "Cophixalus shellyi", "Cophixalus sphagnicola", "Cophixalus verrucosus", "Cophyla phyllodactyla", "Copiula fistulans", "Copiula oxyrhina", "Copiula tyleri", "Ctenophryne geayi", "Dermatonotus muelleri", "Dyscophus guineti", "Dyscophus insularis", "Elachistocleis bicolor", "Elachistocleis ovalis", "Elachistocleis piauiensis", "Elachistocleis surinamensis", "Gastrophryne carolinensis", "Gastrophryne elegans", "Gastrophryne olivacea", "Gastrophryne pictiventris", "Gastrophryne usta", "Genyophryne thomsoni", "Hamptophryne boliviana", "Hylophorbus rufescens", "Hypopachus variolosus", "Kalophrynus heterochirus", "Kalophrynus interlineatus", "Kalophrynus pleurostigma", "Kaloula baleata", "Kaloula borealis", "Kaloula conjuncta", "Kaloula picta", "Kaloula pulchra", "Kaloula rugifera", "Kaloula taprobanica", "Kaloula verrucosa", "Liophryne dentata", "Liophryne schlaginhaufeni", "Mantophryne lateralis", "Metaphrynella pollicaris", "Metaphrynella sundana", "Microhyla achatina", "Microhyla annamensis", "Microhyla berdmorei", "Microhyla borneensis", "Microhyla butleri", "Microhyla heymonsi", "Microhyla mixtura", "Microhyla ornata", "Microhyla palmipes", "Microhyla pulchra", "Microhyla rubra", "Micryletta inornata", "Myersiella microps", "Nelsonophryne aequatorialis", "Nelsonophryne aterrima", "Oreophryne anthonyi", "Oreophryne biroi", "Oreophryne brachypus", "Oreophryne geislerorum", "Oreophryne hypsiops", "Oreophryne inornata", "Oreophryne kapisa", "Otophryne pyburni", "Otophryne robusta", "Otophryne steyermarki", "Oxydactyla alpestris", "Oxydactyla stenodactyla", "Paradoxophyla palmata", "Phrynella pulchra", "Phrynomantis affinis", "Phrynomantis annectens", "Phrynomantis bifasciatus", "Phrynomantis microps", 
		"Phrynomantis somalicus", "Platypelis barbouri", "Platypelis grandis", "Platypelis tuberifera", "Rhombophryne alluaudi", "Plethodontohyla bipunctata", "Plethodontohyla inguinalis", "Rhombophryne laevipes", "Plethodontohyla mihanika", "Plethodontohyla notosticta", "Plethodontohyla ocellata", "Ramanella variegata", "Relictivomer pearsei", "Scaphiophryne brevis", "Scaphiophryne calcarata", "Scaphiophryne spinosa", "Spelaeophryne methneri", "Sphenophryne cornuta", "Stereocyclops incrassatus", "Stereocyclops parkeri", "Stumpffia gimmeli", "Synapturanus mirandaribeiroi", "Synapturanus rabus", "Synapturanus salseri", "Syncope antenori", "Syncope carvalhoi", "Syncope tridactyla", "Uperodon globulosus", "Uperodon systoma", "Xenorhina bidens", "Xenorhina fuscigula", "Xenorhina macrops", "Xenorhina mehelyi", "Xenorhina obesa", "Xenorhina rostrata", "Xenorhina bouwensi", "Xenorhina oxycephala", "Xenorhina parkerorum", "Xenorhina similis", "Pseudophryne raveni", "Pelobates syriacus", "Pelodytes ibericus", "Pelodytes punctatus", "Arthroleptella bicolor", "Anhydrophryne hewitti", "Arthroleptella villiersi", "Cacosternum boettgeri", "Cacosternum namaquense", "Cacosternum nanum", "Cacosternum parvum", "Cacosternum platys", "Phrynobatrachus africanus", "Petropedetes newtoni", "Petropedetes parkeri", "Phrynobatrachus accraensis", "Phrynobatrachus acridoides", "Phrynobatrachus auritus", "Phrynobatrachus batesii", "Phrynobatrachus bullans", "Phrynobatrachus calcaratus", "Phrynobatrachus cornutus", "Phrynobatrachus dendrobates", "Phrynobatrachus dispar", "Phrynobatrachus francisci", "Phrynobatrachus fraterculus", "Phrynobatrachus graueri", "Phrynobatrachus gutturosus", "Phrynobatrachus hylaios", "Phrynobatrachus keniensis", "Phrynobatrachus kinangopensis", "Phrynobatrachus mababiensis", "Phrynobatrachus minutus", "Phrynobatrachus natalensis", "Phrynobatrachus parkeri", "Phrynobatrachus parvulus", "Phrynobatrachus perpalmatus", "Phrynobatrachus plicatus", "Phrynobatrachus rungwensis", "Phrynobatrachus scapularis", "Phrynobatrachus tokba", "Phrynobatrachus werneri", "Phrynobatrachus sandersoni", "Hymenochirus boettgeri", "Hymenochirus curtipes", "Pipa arrabali", "Pipa aspera", "Pipa carvalhoi", "Pipa parva", "Pipa pipa", "Pipa snethlageae", "Pseudhymenochirus merlini", "Silurana epitropicalis", "Silurana tropicalis", "Xenopus andrei", "Xenopus borealis", "Xenopus clivii", "Xenopus fraseri", "Xenopus laevis", "Xenopus muelleri", "Xenopus petersii", "Xenopus pygmaeus", "Xenopus vestitus", "Xenopus wittei", "Amietia angolensis", "Amietia fuscigula", "Hylarana albolabris", "Hylarana amnicola", "Hylarana darlingi", "Hylarana galamensis", "Hylarana lemairei", "Hylarana lepus", "Amolops chunganensis", "Amolops formosus", "Amolops gerbillus", "Amolops granulosus", "Amolops larutensis", "Amolops mantzorum", "Amolops marmoratus", "Amolops monticola", "Amolops ricketti", "Amolops wuyiensis", "Aubria masako", "Aubria occidentalis", "Aubria subsigillata", "Batrachylodes elegans", "Batrachylodes mediodiscus", "Batrachylodes minutus", "Batrachylodes montanus", "Batrachylodes trossulus", "Batrachylodes vertebralis", "Batrachylodes wolfi", "Ceratobatrachus guentheri", "Ombrana sikimensis", "Conraua beccarii", "Conraua crassipes", "Discodeles bufoniformis", "Discodeles guppyi", "Discodeles vogti", "Euphlyctis cyanophlyctis", "Euphlyctis ehrenbergii", "Euphlyctis hexadactylus", "Fejervarya andamanensis", "Fejervarya cancrivora", "Fejervarya iskandari", "Fejervarya keralensis", "Fejervarya kirtisinghei",
		"Fejervarya limnocharis", "Fejervarya nepalensis", "Hylarana nicobariensis", "Fejervarya orissaensis", "Fejervarya pierrei", "Fejervarya rufescens", "Fejervarya syhadrensis", "Fejervarya teraiensis", "Fejervarya verruculosa", "Fejervarya vittigera", "Hildebrandtia macrotympanum", "Hildebrandtia ornata", "Hoplobatrachus crassus", "Hoplobatrachus occipitalis", "Hoplobatrachus rugulosus", "Hoplobatrachus tigerinus", "Huia cavitympanum", "Odorrana nasica", "Huia sumatrana", "Indirana beddomii", "Indirana semipalmata", "Ingerana baluensis", "Ingerana tenasserimensis", "Lankanectes corrugatus", "Limnonectes finchi", "Limnonectes fujianensis", "Limnonectes grunniens", "Limnonectes gyldenstolpei", "Limnonectes hascheanus", "Limnonectes kadarsani", "Limnonectes kuhlii", "Limnonectes laticeps", "Limnonectes leporinus", "Limnonectes microdiscus", "Limnonectes modestus", "Limnonectes palavanensis", "Limnonectes plicatellus", "Limnonectes shompenorum", "Meristogenys orphnocnemis", "Nanorana parkeri", "Nanorana ventripunctata", "Occidozyga celebensis", "Occidozyga laevis", "Occidozyga lima", "Occidozyga magnapustulosa", "Occidozyga martensii", "Occidozyga semipalmata", "Occidozyga sumatrana", "Nanorana blanfordii", "Allopaa hazarensis", "Nanorana liebigii", "Nanorana polunini", "Chrysopaa sternosignata", "Nanorana vicina", "Platymantis aculeodactylus", "Platymantis boulengeri", "Platymantis browni", "Platymantis corrugatus", "Platymantis cryptotis", "Platymantis dorsalis", "Platymantis guppyi", "Platymantis magnus", "Platymantis neckeri", "Platymantis papuensis", "Platymantis pelewensis", "Platymantis punctatus", "Platymantis schmidti", "Platymantis solomonis", "Platymantis weberi", "Rana multidenticulata", "Ptychadena aequiplicata", "Ptychadena anchietae", "Ptychadena ansorgii", "Ptychadena bibroni", "Ptychadena bunoderma", "Ptychadena chrysogaster", "Ptychadena cooperi", "Ptychadena gansi", "Ptychadena grandisonae", "Ptychadena guibei", "Ptychadena keilingi", "Ptychadena longirostris", "Ptychadena mahnerti", "Ptychadena mascareniensis", "Ptychadena mossambica", "Ptychadena neumanni", "Ptychadena obscura", "Ptychadena oxyrhynchus", "Ptychadena perplicata", "Ptychadena perreti", "Ptychadena porosissima", "Ptychadena pumilio", "Ptychadena schillukorum", "Ptychadena stenocephala", "Ptychadena straeleni", "Ptychadena subpunctata", "Ptychadena taenioscelis", "Ptychadena tournieri", "Ptychadena trinodis", "Ptychadena upembae", "Ptychadena uzungwensis", "Pyxicephalus adspersus", "Pyxicephalus edulis", "Pyxicephalus obbianus", "Babina adenopleura", "Clinotarsus alticola", "Rana amurensis", "Odorrana andersonii", "Amolops archotaphus", "Hylarana arfaki", "Rana arvalis", "Rana asiatica", "Rana aurora", "Hylarana baramica", "Pelophylax bedriagae", "Pelophylax bergeri", "Lithobates berlandieri", "Lithobates blairi", "Lithobates catesbeianus", "Hylarana celebensis", "Hylarana chalconota", "Rana chaochiaoensis", "Babina chapaensis", "Rana chensinensis", "Odorrana chloronota", "Lithobates clamitans", "Hylarana cubitalis", "Rana dalmatina", "Babina daunchina", "Rana dybowskii", "Hylarana elberti", "Glandirana emeljanovi", "Hylarana erythraea", "Pelophylax esculentus", "Odorrana exiliversabilis", "Hylarana faber", "Hylarana florensis", "Lithobates forreri", "Pelophylax fukienensis", "Hylarana garoensis", "Hylarana garritor", "Hylarana glandulosa", "Hylarana gracilis", "Rana graeca", "Hylarana grandocula", "Lithobates grylio", "Hylarana guentheri", "Lithobates heckscheri", "Pelophylax hispanicus", "Odorrana hosii", "Rana huanrensis", "Pelophylax hubeiensis", "Humerana humeralis", "Rana italica", "Rana japonica", "Hylarana jimiensis", "Rana johnsi", "Hylarana kampeni", "Hylarana kreffti", "Rana kukunoris", "Pelophylax kurtmuelleri", 
		"Pelophylax lateralis", "Hylarana laterimaculata", "Hylarana latouchii", "Hylarana leptoglossa", "Pelophylax lessonae", "Hylarana luctuosa", "Rana luteiventris", "Rana macrocnemis", "Hylarana macrodactyla", "Lithobates maculatus", "Lithobates magnaocularis", "Hylarana malabarica", "Hylarana maosonensis", "Odorrana margaretae", "Hylarana milleti", "Humerana miopus", "Hylarana mocquardii", "Hylarana moluccana", "Lithobates montezumae", "Hylarana montivaga", "Odorrana morafkai", "Hylarana nigrovittata", "Hylarana novaeguineae", "Rana omeimontis", "Rana ornativentris", "Lithobates palmipes", "Lithobates palustris", "Hylarana papua", "Pelophylax perezi", "Hylarana picturata", "Lithobates pipiens", "Rana pirica", "Pelophylax plancyi", "Babina pleuraden", "Pelophylax porosus", "Lithobates pustulosus", "Hylarana raniceps", "Pelophylax ridibundus", "Glandirana rugosa", "Pelophylax saharicus", "Rana sakuraii", "Sanguirana sanguinea", "Odorrana schmackeri", "Lithobates septentrionalis", "Rana shuchinae", "Hylarana siberu", "Hylarana signata", "Lithobates spectabilis", "Lithobates sphenocephalus", "Hylarana supragrisea", "Odorrana swinhoana", "Lithobates sylvaticus", "Rana tagoi", "Hylarana taipehensis", "Lithobates taylori", "Rana temporaria", "Odorrana tiannanensis", "Rana tsushimensis", "Hylarana tytleri", "Lithobates vaillanti", "Odorrana versabilis", "Lithobates virgatipes", "Lithobates warszewitschii", "Rana zhenhaiensis", "Lithobates zweifeli", "Sphaerotheca breviceps", "Sphaerotheca dobsonii", "Sphaerotheca maskeyi", "Sphaerotheca rolandae", "Staurois latopalmatus", "Staurois natator", "Strongylopus bonaespei", "Strongylopus fasciatus", "Strongylopus fuelleborni", "Strongylopus grayii", "Strongylopus hymenopus", "Tomopterna cryptotis", "Tomopterna delalandii", "Tomopterna krugerensis", "Tomopterna marmorata", "Tomopterna natalensis", "Tomopterna tandyi", "Tomopterna tuberculosa", "Buergeria buergeri", "Buergeria japonica", "Buergeria robusta", "Chiromantis doriae", "Chiromantis nongkhorensis", "Chiromantis simus", "Chiromantis vittatus", "Chiromantis kelleri", "Chiromantis petersii", "Chiromantis rufescens", "Chiromantis xerampelina", "Kurixalus eiffingeri", "Kurixalus idiootocus", "Philautus abundus", "Philautus andersoni", "Philautus annandalii", "Philautus aurifasciatus", "Philautus fergusonianus", "Gracixalus gracilipes", "Philautus hoipolloi", "Philautus longchuanensis", "Kurixalus odontotarsus", "Philautus parvulus", "Philautus petersi", "Philautus popularis", "Philautus anili", "Philautus surdus", "Philautus vermiculatus", "Rhacophorus chenfui", "Polypedates colletti", "Polypedates cruciger", "Rhacophorus dugritei", "Rhacophorus feae", "Polypedates leucomystax", "Polypedates macrotis", "Polypedates maculatus", "Polypedates megacephalus", "Polypedates mutus", "Rhacophorus omeimontis", "Polypedates otilophus", "Polypedates pseudocruciger", "Polypedates taeniatus", "Rhacophorus appendiculatus", "Rhacophorus arboreus", "Rhacophorus bipunctatus", "Kurixalus bisacculus", "Rhacophorus cyanopunctatus", "Rhacophorus dennysi", "Rhacophorus malabaricus", "Rhacophorus maximus", "Rhacophorus moltrechti", "Rhacophorus nigropalmatus", "Rhacophorus owstoni", "Rhacophorus pardalis", "Rhacophorus prominanus", "Rhacophorus schlegelii", "Kurixalus verrucosus", "Rhacophorus viridis", "Theloderma asperum", "Theloderma gordoni", "Theloderma horridum", "Theloderma leporosum", "Rhinophrynus dorsalis", "Scaphiopus couchii", "Scaphiopus holbrookii", "Scaphiopus hurterii", "Spea bombifrons",
		"Spea intermontana", "Spea multiplicata", "Ambystoma annulatum", "Ambystoma gracile", "Ambystoma jeffersonianum", "Ambystoma laterale", "Ambystoma mabeei", "Ambystoma macrodactylum", "Ambystoma maculatum", "Ambystoma opacum", "Ambystoma rosaceum", "Ambystoma talpoideum", "Ambystoma texanum", "Ambystoma tigrinum", "Ambystoma velasci", "Amphiuma means", "Amphiuma tridactylum", "Dicamptodon aterrimus", "Dicamptodon copei", "Dicamptodon tenebrosus", "Hynobius kimurae", "Hynobius leechii", "Hynobius lichenatus", "Hynobius nebulosus", "Hynobius nigrescens", "Hynobius retardatus", "Hynobius tsuensis", "Onychodactylus japonicus", "Salamandrella keyserlingii", "Aneides hardii", "Aneides lugubris", "Batrachoseps attenuatus", "Batrachoseps gavilanensis", "Batrachoseps gregarius", "Batrachoseps luciae", "Batrachoseps major", "Batrachoseps nigriventris", "Batrachoseps pacificus", "Bolitoglossa adspersa", "Bolitoglossa alberchi", "Bolitoglossa altamazonica", "Bolitoglossa biseriata", "Bolitoglossa cerroensis", "Bolitoglossa colonnea", "Bolitoglossa equatoriana", "Bolitoglossa mexicana", "Bolitoglossa morio", "Bolitoglossa nicefori", "Bolitoglossa occidentalis", "Bolitoglossa peruviana", "Bolitoglossa ramosi", "Bolitoglossa robusta", "Bolitoglossa rufescens", "Bolitoglossa schizodactyla", "Bolitoglossa striatula", "Bolitoglossa vallecula", "Bolitoglossa yucatana", "Desmognathus apalachicolae", "Desmognathus auriculatus", "Desmognathus brimleyorum", "Desmognathus carolinensis", "Desmognathus fuscus", "Desmognathus imitator", "Desmognathus marmoratus", "Desmognathus monticola", "Desmognathus ochrophaeus", "Desmognathus ocoee", "Desmognathus orestes", "Desmognathus quadramaculatus", "Desmognathus santeetlah", "Desmognathus welteri", "Desmognathus wrighti", "Ensatina eschscholtzii", "Eurycea bislineata", "Eurycea cirrigera", "Eurycea guttolineata", "Eurycea longicauda", "Eurycea lucifuga", "Eurycea multiplicata", "Eurycea quadridigitata", "Eurycea wilderae", "Gyrinophilus porphyriticus", "Hemidactylium scutatum", "Hydromantes platycephalus", "Nototriton abscondens", "Oedipina alleni", "Oedipina complex", "Oedipina cyclocauda", "Oedipina elongata", "Oedipina pacificensis", "Oedipina parvipes", "Oedipina taylori", "Plethodon albagula", "Plethodon angusticlavius", "Plethodon cinereus", "Plethodon cylindraceus", "Plethodon dorsalis", "Plethodon dunni", "Plethodon electromorphus", "Plethodon glutinosus", "Plethodon hoffmani", "Plethodon idahoensis", "Plethodon kentucki", "Plethodon kisatchie", "Plethodon metcalfi", "Plethodon montanus", "Plethodon richmondi", "Plethodon serratus", "Plethodon teyahalee", "Plethodon vandykei", "Plethodon vehiculum", "Plethodon ventralis", "Plethodon websteri", "Plethodon wehrlei", "Plethodon yonahlossee", "Pseudotriton montanus", "Pseudotriton ruber", "Stereochilus marginatus", "Necturus beyeri", "Necturus maculosus", "Necturus punctatus", "Rhyacotriton variegatus", "Cynops cyanurus", "Cynops orientalis", "Cynops pyrrhogaster", "Euproctus montanus", "Notophthalmus viridescens", "Pachytriton brevipes", "Pachytriton labiatus", "Paramesotriton chinensis", "Salamandra corsica", "Salamandra salamandra", "Salamandrina terdigitata", "Taricha granulosa", "Taricha rivularis", "Taricha torosa", "Mesotriton alpestris", "Lissotriton boscai", "Triturus carnifex", "Lissotriton helveticus", "Lissotriton italicus", "Triturus marmoratus", "Lissotriton montandoni", "Ommatotriton vittatus", "Lissotriton vulgaris", "Tylototriton verrucosus", "Pseudobranchus axanthus", "Pseudobranchus striatus", "Siren intermedia", "Siren lacertina", "Boulengerula boulengeri", "Boulengerula taitana", "Boulengerula uluguruensis", "Brasilotyphlus braziliensis", "Caecilia disossea", "Caecilia gracilis", "Caecilia leucocephala", 
		"Caecilia marcusi", "Caecilia nigricans", "Caecilia orientalis", "Caecilia perdita", "Caecilia subdermalis", "Caecilia subnigricans", "Caecilia tentaculata", "Chthonerpeton indistinctum", "Dermophis parviceps", "Gegeneophis ramaswamii", "Geotrypetes seraphini", "Grandisonia alternans", "Grandisonia larvata", "Grandisonia sechellensis", "Gymnopis multiplicata", "Herpele squalostoma", "Hypogeophis rostratus", "Microcaecilia albiceps", "Microcaecilia taylori", "Microcaecilia unicolor", "Nectocaecilia petersii", "Oscaecilia bassleri", "Oscaecilia ochrocephala", "Parvicaecilia nicefori", "Parvicaecilia pricei", "Potomotyphlus kaupii", "Schistometopum gregorii", "Schistometopum thomense", "Siphonops annulatus", "Siphonops hardyi", "Siphonops paulensis", "Typhlonectes compressicauda", "Typhlonectes natans", "Ichthyophis bannanicus", "Ichthyophis beddomei", "Ichthyophis glutinosus", "Ichthyophis kohtaoensis", "Ichthyophis tricolor", "Epicrionops bicolor", "Epicrionops niger", "Epicrionops petersi", "Rhinatrema bivittatum", "Scolecomorphus kirkii", "Scolecomorphus uluguruensis", "Scolecomorphus vittatus", "Allophryne ruthveni", "Arthroleptis adelphus", "Rhinella abei", "Incilius aucoinae", "Rhinella henseli", "Rhinella ornata", "Rhinella pombali", "Incilius signifer", "Allobates pittieri", "Bokermannohyla caramaschii", "Dendropsophus coffeus", "Hypsiboas pombali", "Litoria auae", "Litoria wilcoxii", "Scinax constrictus", "Hyperolius adspersus", "Hyperolius camerunensis", "Hyperolius igbettensis", "Oreobates madidi", "Gastrotheca piperata", "Oreobates sanderi", "Physalaemus erikae", "Guibemantis timidus", "Leptobrachium huashen", "Breviceps fichus", "Microhyla marmorata", "Nyctibatrachus petraeus", "Rana pseudodalmatina", "Tomopterna luganga", "Karsenia koreana", "Pseudoeurycea mixteca", "Bokermannohyla itapoty", "Cophixalus humicola", "Pristimantis cruciocularis", "Pristimantis ornatus", "Pristimantis aureolineatus", "Litoria staccato", "Mixophyes carbinensis", "Pristimantis saltissimus", "Mantella ebenaui", "Bokermannohyla oxente", "Leptodactylus sertanejo", "Rhinella veredas", "Litoria rivicola", "Pristimantis dendrobatoides", "Leptodactylus paraensis", "Pseudacris fouquettei", "Ameerega yungicola", "Leptolalax melanolecus", "Microhyla fissipes", "Microhyla okinavensis", "Colostethus ucumari", "Rana coreana", "Anomaloglossus kaiei", "Dendropsophus juliani", "Limnonectes poilani", "Hypsiboas jimenezi", "Pristimantis bellator", "Litoria humboldtorum", "Mantidactylus bellyi", "Amolops panhai", "Leptodactylus nanus", "Pseudacris maculata", "Xenopus victorianus", "Pristimantis aquilonaris", "Taricha sierra", "Theloderma licin", "Hyperolius poweri", "Ischnocnema henselii", "Mixophyes coggeri", "Hylarana milneana", "Rhacophorus rhodopus", "Ranitomeya uakarii", "Leptodactylus rhodomerus", "Cacosternum plimptoni", "Rhinella martyi", "Rhinella magnussoni", "Hyalinobatrachium tatayoi", "Leptodactylus savagei", "Leptodactylus vastus", "Platymantis admiraltiensis", "Microhyla mantheyi", "Rhacophorus suffry", "Hypsiboas curupi", "Allobates niputidea", "Rhinella achavali", "Phrynobatrachus scheffleri", "Cophixalus variabilis", "Pseudacris kalmi", "Salamandrina perspicillata", "Phrynobatrachus leveleve", "Amolops mengyangensis", "Rhinella hoogmoedi", "Sphaenorhynchus caramaschii", "Pristimantis corrugatus", "Hyperolius substriatus", "Physalaemus freibergi", "Platymantis latro", "Oreobates lehri", "Allobates granti", "Hypsiboas liliae", "Phrynobatrachus pallidus", "Pristimantis jester", "Leptodactylus thomei", "Hypsiboas nympha", "Litoria nudidigita", "Pseudepidalea balearica", "Pseudepidalea boulengeri", "Pseudepidalea sicula", "Pseudepidalea turanensis", "Pseudepidalea viridis"};
//	private static String [] newLCAmphibs = new String [] { "Adelophryne gutturosa", "Afrixalus dorsalis", "Afrixalus osorioi", "Afrixalus vittiger", "Afrixalus weidholzi", "Amietophrynus garmani", "Amietophrynus superciliaris", "Arthroleptis poecilonotus", "Astylosternus occidentalis", "Babina chapaensis", "Bolitoglossa hypacra", "Boulengerula taitana", "Bufo mauritanicus", "Bufo pentoni", "Calluella guttulata", "Chiasmocleis avilapiresae", "Chiromantis doriae", "Chiromantis rufescens", "Cophixalus hosmeri", "Eleutherodactylus coqui", "Eleutherodactylus planirostris", "Fejervarya cancrivora", "Fejervarya keralensis", "Fejervarya limnocharis", "Fejervarya nepalensis", "Fejervarya pierrei", "Fejervarya rufescens", "Fejervarya syhadrensis", "Fejervarya teraiensis", "Hildebrandtia ornata", "Hylarana chalconota", "Hylarana erythraea", "Hylarana guentheri", "Hylarana leptoglossa", "Hymenochirus curtipes", "Hynobius naevius", "Hynobius yatsui", "Hyperolius baumanni", "Hyperolius fusciventris", "Hyperolius guttulatus", "Hyperolius nitidulus", "Hyperolius spinigularis", "Hyperolius sylvaticus", "Ichthyophis kohtaoensis", "Ingerophrynus galeatus", "Kaloula assamensis", "Kaloula pulchra", "Kassina fusca", "Leptobrachella mjobergi", "Leptobrachium chapaense", "Leptobrachium hasseltii", "Leptobrachium smithi", "Leptolalax oshanensis", "Leptopelis bufonides", "Limnonectes laticeps", "Litoria christianbergmanni", "Litoria fallax", "Microhyla berdmorei", "Microhyla butleri", "Microhyla pulchra", "Micryletta inornata", "Occidozyga lima", "Odorrana andersonii", "Odorrana chloronota", "Pelophylax lateralis", "Phrynobatrachus calcaratus", "Phrynobatrachus francisci", "Phrynobatrachus gutturosus", "Phrynobatrachus latifrons", "Phrynobatrachus plicatus", "Physalaemus henselii", "Pipa aspera", "Platymantis magnus", "Polypedates megacephalus", "Pristimantis achuar", "Pristimantis kichwarum", "Psychrophrynella iatamasi", "Psychrophrynella katantika", "Ptychadena aequiplicata", "Ptychadena bibroni", "Ptychadena schillukorum", "Ptychadena taenioscelis", "Ptychadena tellinii", "Ptychadena trinodis", "Pyxicephalus edulis", "Rhacophorus margaritifer", "Rhacophorus orlovi", "Rhacophorus rhodopus", "Stefania ackawaio", "Stefania ayangannae", "Stefania coxi", "Trachycephalus dibernardoi", "Tylototriton verrucosus", "Uperodon systoma", "Xenophrys palpebralespinosa", "Xenopus laevis" };
	private static HashMap<String, String> amphibNames;
	
	public static void countAmphibianAssessments(VFS vfs) throws Exception {
		Document nameDoc = TaxonomyDocUtils.getTaxonomyDocByName();
		Element amphibia = (Element)nameDoc.getElementsByTagName("AMPHIBIA").item(0);
		NodeCollection children = new NodeCollection(amphibia.getChildNodes());
		System.out.println("AMPHIBIA has " + children.size() + " children.");
		int depth = TaxonNode.ORDER;
		
		lcAmphibs = 0;
		crAmphibs = 0;
		newAmphibs = 0;
		amphibNames = new HashMap<String, String>();
		for( String cur : newLCAmphibs )
			amphibNames.put(cur, cur);
		
		peruseChildren(vfs, children, depth);
		
		System.out.println("Least concerned amphibians: " + lcAmphibs);
		System.out.println("CR amphibians: " + crAmphibs);
		System.out.println("2009.1. amphibians unaccounted for: " + amphibNames.size());
		for( Entry<String, String> cur : amphibNames.entrySet() )
			System.out.println(cur.getKey());
	}

	private static void peruseChildren(VFS vfs, NodeCollection children, int depth) {
		for( Node cur : children ) {
			if( cur.getNodeType() == Node.ELEMENT_NODE ) {
				if( depth >= TaxonNode.SPECIES )
					doCountAmphibianAssessments(vfs, (Element)cur);
				
				NodeList temp = cur.getChildNodes();
				if( temp.getLength() > 0 )
					peruseChildren(vfs, new NodeCollection(temp), (depth+1));
			}
		}
	}
	
	private static void doCountAmphibianAssessments(VFS vfs, Element speciesEl) {
		TaxonNode species = TaxaIO.readNode(speciesEl.getAttribute("id"), vfs);
		if( !species.getStatus().equalsIgnoreCase("S") && !species.getStatus().equalsIgnoreCase("D") && species.getAssessments().size() > 0 ) {
			List<AssessmentData> list = AssessmentIO.readList(vfs, species.getAssessmentsCSV(), BaseAssessment.PUBLISHED_ASSESSMENT_STATUS, "", false);
			MostRecentFlagger.flagMostRecentInList(list);
			
			for( AssessmentData cur : list ) {
				if( !cur.isHistorical() && (cur.isGlobal() || cur.isEndemic()) ) {
					if( cur.getManualCategoryAbbreviation().equals("LC")) {
						lcAmphibs++;
						
						if( amphibNames.containsKey(species.getFullName()))
							amphibNames.remove(species.getFullName());
						
					} else if( cur.getManualCategoryAbbreviation().equals("CR"))
						crAmphibs++;
					
					return;
				}
			}
		}
	}
	
	
	
	public static void fixTaxonomicAuthorities(VFS vfs) throws Exception {
		registerDatasource("rldb", "jdbc:access:////usr/data/rldbRelationshipFree.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");
		switchToDBSession("rldb");
		
		Document nameDoc = TaxonomyDocUtils.getTaxonomyDocByName();
		Element mammalia = (Element)nameDoc.getElementsByTagName("MAMMALIA").item(0);
		NodeCollection children = new NodeCollection(mammalia.getChildNodes());
		System.out.println("MAMMALIA has " + children.size() + " children.");
		int depth = TaxonNode.ORDER;
		
		lookAtChildren(vfs, children, depth);
		
		System.out.println(sameNamed.toString());
		System.out.println("\n\n\n");
		System.out.println(suspicious.toString());		
	}

	private static void lookAtChildren(VFS vfs, NodeCollection children, int depth) {
		for( Node cur : children ) {
			if( cur.getNodeType() == Node.ELEMENT_NODE ) {
				if( depth == TaxonNode.SPECIES )
					fixSpeciesTaxonomicAuthorities(vfs, (Element)cur);
				else
					lookAtChildren(vfs, new NodeCollection(cur.getChildNodes()), (depth+1));
			}
		}
	}
	
	private static void fixSpeciesTaxonomicAuthorities(VFS vfs, Element speciesEl) {
		NodeCollection children = new NodeCollection(speciesEl.getChildNodes());
		if( children.size() > 0 ) {
			TaxonNode species = TaxaIO.readNode(speciesEl.getAttribute("id"), vfs);
			for( Node cur : children ) {
				if( cur.getNodeType() == Node.ELEMENT_NODE ) {
					String id = ((Element)cur).getAttribute("id");
					TaxonNode infrarank = TaxaIO.readNode(id, vfs);
					
					if( infrarank.getLevel() != TaxonNode.SUBPOPULATION ) {
						if( species.getName().equals(infrarank.getName()) ) {
							if( !species.getTaxonomicAuthority().equalsIgnoreCase(infrarank.getTaxonomicAuthority() ) ) {
								sameNamed.append("\"" + species.getId() + "\",\"" + species.getFullName() + "\",\"" + species.getTaxonomicAuthority());
								sameNamed.append("\",\"" + infrarank.getTaxonomicAuthority() + "\",\"" + infrarank.getId() + "\",\"" + infrarank.getFullName() + "\"\r\n");
								
								species.setTaxonomicAuthority(infrarank.getTaxonomicAuthority());
								TaxaIO.writeNode(species, vfs);
//								System.out.println(species.getFullName() + " is named the same as " + infrarank.getFullName());
//								System.out.println(species.getTaxonomicAuthority() + " and " + infrarank.getTaxonomicAuthority());
//								System.out.println("--------------------------------------------------------");
							}
						} else if( (species.getTaxonomicAuthority() != null && infrarank.getTaxonomicAuthority() != null) && 
							species.getTaxonomicAuthority().equalsIgnoreCase(infrarank.getTaxonomicAuthority())) {
							
							SelectQuery select = new SelectQuery();
							select.select("Species", "SpcRecID");
							select.select("Species", "SpcAuthor");
							select.select("Species", "SpcInfraRankAuthor");
							select.constrain(new CanonicalColumnName("Species", "SpcRecID"), QConstraint.CT_EQUALS, ""+species.getId());

							try {
								Row.Loader loader = new Row.Loader();
								ec.doQuery(select, loader);
								
								String authority = loader.getRow().get("SpcAuthor").getString(Column.NEVER_NULL);
								
								suspicious.append("\"" + species.getId() + "\",\"" + species.getFullName() + "\",\"" + species.getTaxonomicAuthority() + "\",\"" + authority);
								suspicious.append("\",\"" + infrarank.getId() + "\",\"" + infrarank.getFullName() + "\"\r\n");
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							
//							System.out.println(species.getFullName() + " HAS THE SAME AUTHORITY AS " + infrarank.getFullName());
//							System.out.println("--------------------------------------------------------");
						}
					}
				}
			}
		}
	}
	
	public static void findDupeNamedTaxa() throws NotFoundException {
		try {
			Document nameDoc = TaxonomyDocUtils.getTaxonomyDocByName();
			NodeList list = nameDoc.getDocumentElement().getChildNodes();
			doFindDupeTaxa(new HashMap<String, String>(), nameDoc, list, 0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int doFindDupeTaxa(HashMap<String, String> dupesFound, Document nameDoc, NodeList list, int total) {
		for( int i = 0; i < list.getLength(); i++ ) {
			Node node = list.item(i);
			if( node.getNodeType() != Node.ELEMENT_NODE )
				continue;
			
			String name = node.getNodeName();
			if( !dupesFound.containsKey(name) ) {
				NodeList els = nameDoc.getElementsByTagName(name);
				if( els.getLength() > 1 ) {
					dupesFound.put(name, name);

					if( els.getLength() == 2 ) {
						String docElName = nameDoc.getDocumentElement().getNodeName();
						String one = getKingdomName(els.item(0), docElName);
						String two = getKingdomName(els.item(1), docElName);
						
						if( one.equals(two) )
							System.out.println("Possibly " + els.getLength() + " dupes for taxon " + name + " - checked Kingdom already.");
					} else
						System.out.println("Possibly " + els.getLength() + " dupes for taxon " + name);
				}
			}
			total++;
			
			if( total % 1000 == 0 )
				System.out.println("Through " + total + " taxa.");
			
			NodeList children = node.getChildNodes();
			if( children.getLength() > 0 )
				total = doFindDupeTaxa(dupesFound, nameDoc, children, total);
		}
		
		return total;
	}

	private static String getKingdomName(Node node, String docElementName) {
		Node cur = node.getParentNode();
		while( !cur.getParentNode().getNodeName().equals(docElementName) )
			cur = cur.getParentNode();
		
		return cur.getNodeName();
	}
	
	public static void lookForDupeAssessments(VFS vfs, String rootPath) throws NotFoundException {
		try {
			VFSPath path = VFSUtils.parseVFSPath(rootPath);
			for (VFSPathToken url : vfs.list(path)) {
				String curURL = rootPath + url;
				VFSPath curPath = VFSUtils.parseVFSPath(curURL);
				if (vfs.isCollection(curPath))
					lookForDupeAssessments(vfs, curURL + "/");
				else
					doLookForDupeAssessments(vfs, curURL);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void doLookForDupeAssessments(VFS vfs, String path) throws NotFoundException {
		if( path.endsWith(".xml") ) {
			Document doc = DocumentUtils.getVFSFileAsDocument(path, vfs);
			NodeList list = doc.getElementsByTagName("assessments");
			if( list.getLength() > 0 ) {
				String ids = list.item(0).getTextContent();
				if( ids.contains("38949") )
					System.out.println("Path " + path + " contains ID string " + ids);
			}
		}
	}
		
	public static void changeInfrarankType(VFS vfs, String rootPath) throws NotFoundException {
		try {
			VFSPath path = VFSUtils.parseVFSPath(rootPath);
			for (VFSPathToken url : vfs.list(path)) {
				String curURL = rootPath + url;
				VFSPath curPath = VFSUtils.parseVFSPath(curURL);
				if (vfs.isCollection(curPath))
					changeInfrarankType(vfs, curURL + "/");
				else
					doChangeInfrarankType(vfs, curURL);
			}

		}

		catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void changeISOCode(VFS vfs, String rootPath) throws NotFoundException {
		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL))
				changeISOCode(vfs, curURL + "/");
			else
				doChangeISOCode(vfs, curURL);
		}
	}

	private static void changeStatus(VFS vfs, String path) throws NotFoundException {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(path, vfs), null, false);

		if (node.isDeprecated()) {
			log.append(node.generateFullName() + " -- " + node.getId() + ". Old status: " + node.getStatus() + "\n");

			// node.setDeprecated(false);
			node.setStatus("S");

			DocumentUtils.writeVFSFile(path, vfs, TaxonNodeFactory.nodeToDetailedXML(node));

			changed++;
		}
	}

	public static void changeTaxonStatus(VFS vfs, String rootPath) throws NotFoundException {
		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL))
				changeTaxonStatus(vfs, curURL + "/");
			else
				changeStatus(vfs, curURL);
		}
	}

	public static void dedupeCommonNames(VFS vfs, String rootPath) throws NotFoundException {
		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL))
				dedupeCommonNames(vfs, curURL + "/");
			else
				doDedupe(vfs, curURL);
		}
	}

	public static void deleteAssessments(VFS vfs) throws Exception {
		registerDatasource("ref", "jdbc:access:////usr/data/deleteAssessments.mdb", "com.hxtt.sql.access.AccessDriver",
				"", "");
		switchToDBSession("ref");

		SelectQuery sel = new SelectQuery();
		sel.select("_ASSESSMENTS TO DELETE", "*");

		Row.Set set = new Row.Set();
		ec.doQuery(sel, set);

		if (!vfs.exists("/browse/deleted_assessments"))
			vfs.makeCollection("/browse/deleted_assessments");

		for (Row curRow : set.getSet()) {
			String taxID = curRow.get("tax_id").getString();
			String assessID = curRow.get("id").getString();

			String taxonPath = ServerPaths.getURLForTaxa(taxID);
			String assessPath = ServerPaths.getPublishedAssessmentURL(assessID);

			if (!vfs.exists(assessPath))
				continue;

			TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(taxonPath, vfs), null, false);

			node.removeAssessment(assessID);
			writebackTaxon(node, vfs);

			vfs.move(assessPath, "/browse/deleted_assessments/" + assessID + ".xml");
			System.out.println("Deleted assessment " + assessID);
		}
	}

	private static void doChangeInfrarankType(VFS vfs, String path) {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		Document file = DocumentUtils.getVFSFileAsDocument(path, vfs);
		Element docEl = file.getDocumentElement();

		String level = docEl.getAttribute("level");
		String iType = docEl.getAttribute("infrarankType");

		if (level.equals(String.valueOf(TaxonNode.INFRARANK)) && (iType == null || iType.equals(""))) {
			// System.out.println("Modding " + docEl.getAttribute("id"));
			String name = docEl.getAttribute("name");
			int type = TaxonNode.INFRARANK_TYPE_NA;
			if (name.startsWith("ssp.")) {
				name = name.substring(5);
				type = TaxonNode.INFRARANK_TYPE_SUBSPECIES;
			} else if (name.startsWith("var.")) {
				name = name.substring(5);
				type = TaxonNode.INFRARANK_TYPE_VARIETY;
			}
			docEl.setAttribute("name", name);
			docEl.setAttribute("infrarankType", String.valueOf(type));

			DocumentUtils.writeVFSFile(path, vfs, file);
		}

	}

	private static void doChangeISOCode(VFS vfs, String path) {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		Document file = DocumentUtils.getVFSFileAsDocument(path, vfs);
		NodeCollection commonNames = new NodeCollection(file.getDocumentElement().getElementsByTagName("commonName"));

		for (Node curName : commonNames) {
			String lang = ((Element) curName).getAttribute("language");

			if (lang.equalsIgnoreCase("Spanish")) {
				((Element) curName).setAttribute("iso", "spa");
				((Element) curName).setAttribute("language", "Spanish; Castilian");
				DocumentUtils.writeVFSFile(path, vfs, file);
			}
		}
	}

	private static void doDedupe(VFS vfs, String path) throws NotFoundException {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(path, vfs), null, false);

		ArrayList<CommonNameData> commonNames = node.getCommonNames();
		HashMap<String, CommonNameData> map = new HashMap<String, CommonNameData>();

		boolean writeback = false;

		for (CommonNameData curCN : commonNames) {
			try {
				String normalizedName = normalizeName(curCN.getName());

				if (normalizedName == null)
					continue;

				if (!curCN.getName().equals(normalizedName)) {
					curCN.setName(normalizedName);
					writeback = true;
				}
			} catch (Exception e) {
				System.out.println("Exception working on taxon " + node.getId());
				e.printStackTrace();
				System.exit(0);
			}

			if (curCN.getIsoCode().trim().length() == 2) {
				if (curCN.getIsoCode().equalsIgnoreCase("en"))
					curCN.setIsoCode("eng");
				else if (curCN.getIsoCode().equalsIgnoreCase("es"))
					curCN.setIsoCode("spa");
				else if (curCN.getIsoCode().equalsIgnoreCase("fr"))
					curCN.setIsoCode("fre");

				writeback = true;
			}

			if (!map.containsKey(curCN.getName()))
				map.put(curCN.getName(), curCN);
			else {
				CommonNameData extant = map.get(curCN.getName());

				boolean primary = curCN.isPrimary() || extant.isPrimary();
				ArrayList notes = new ArrayList();

				if (extant.getNotes() != null)
					notes.addAll(extant.getNotes());
				if (curCN.getNotes() != null)
					notes.addAll(curCN.getNotes());

				if (primary != extant.isPrimary()) {
					extant.setPrimary(primary);
					writeback = true;
				}

				if (notes.size() != (extant.getNotes() == null ? 0 : extant.getNotes().size())) {
					System.out.println("Taxon " + node.getId() + " has notes.");
					extant.setNotes(notes);
					writeback = true;
				}
			}
		}
		if (writeback) {
			System.out.println("Changed " + node.getId());
			writebackTaxon(node, vfs);
		}

		if (map.values().size() != node.getCommonNames().size() || writeback) {
			ArrayList<CommonNameData> deduped = new ArrayList<CommonNameData>(map.values());
			node.setCommonNames(deduped);

			for (CommonNameData cur : deduped)
				cur.setValidated(true, CommonNameData.UNSET);

			System.out.println("Changed " + node.getId());
			writebackTaxon(node, vfs);
		}
	}

	private static void doFixTaxonomicAuthority(VFS vfs, String path) throws NotFoundException {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		taxaCount++;

		String xml = DocumentUtils.getVFSFileAsString(path, vfs);
		TaxonNode curNode = null;

		try {
			curNode = TaxonNodeFactory.createNode(xml, null, false);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Error creating node at path " + path);
			return;
		}

		if (curNode.getLevel() >= TaxonNode.SPECIES) {
			if (curNode.getTaxonomicAuthority() == null || curNode.getTaxonomicAuthority().equals("")) {
				SelectQuery select = new SelectQuery();
				select.select("Species", "SpcRecID");
				select.select("Species", "SpcAuthor");
				select.select("Species", "SpcInfraRankAuthor");
				select.constrain(new CanonicalColumnName("Species", "SpcRecID"), QConstraint.CT_EQUALS, curNode.getId()
						+ "");

				try {
					Row.Loader loader = new Row.Loader();
					ec.doQuery(select, loader);

					if (loader.getRow() != null) {
						String authority = "";

						if (curNode.getLevel() == TaxonNode.SPECIES || curNode.getLevel() == TaxonNode.SUBPOPULATION)
							authority = loader.getRow().get("SpcAuthor").getString(Column.NEVER_NULL);
						else if (curNode.getLevel() == TaxonNode.INFRARANK
								|| curNode.getLevel() == TaxonNode.INFRARANK_SUBPOPULATION)
							authority = loader.getRow().get("SpcInfraRankAuthor").getString(Column.NEVER_NULL);

						if (!authority.equals("")) {
							// System.out.println(
							// "Changing taxonomic authority for "
							// + curNode.getId() +
							// " to " + authority);
							curNode.setTaxonomicAuthority(authority);
							DocumentUtils.writeVFSFile(path, vfs, TaxonNodeFactory.nodeToDetailedXML(curNode));

							changed++;
						}
					}
				} catch (DBException e) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void doKillAllButBeetles(VFS vfs, String path) throws NotFoundException, ConflictException {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		taxaCount++;

		String xml = DocumentUtils.getVFSFileAsString(path, vfs);
		TaxonNode curNode = null;

		try {
			curNode = TaxonNodeFactory.createNode(xml, null, false);
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Error creating node at path " + path);
			return;
		}

		if (footprint.contains(curNode.getName())
				|| (curNode.getLevel() >= TaxonNode.GENUS && Arrays.deepEquals(Arrays16Emulation.copyOf(curNode
						.getFootprint(), 5), footprint.toArray(new String[0])))) {
			// Scarab!
		} else
			vfs.delete(path);

	}

	private static void doNormalize(VFS vfs, String path) throws NotFoundException {
		if (!path.endsWith(".xml")) {
			System.out.println("Given a non-XML path to mod: " + path);
			return;
		}

		TaxonNode node = TaxonNodeFactory.createNode(DocumentUtils.getVFSFileAsString(path, vfs), null, false);

		ArrayList<CommonNameData> commonNames = node.getCommonNames();
		HashMap<String, CommonNameData> map = new HashMap<String, CommonNameData>();

		boolean writeback = false;

		Pattern p = Pattern.compile("(.*\\w)'S(\\s.*)");

		for (CommonNameData curCN : commonNames) {
			try {
				String normalizedName = curCN.getName();

				if (normalizedName == null)
					continue;

				// System.out.println("Working on common name " +
				// normalizedName);
				if (p.matcher(normalizedName).matches()) {
					normalizedName = normalizedName.replaceAll("(.*\\w)'S(\\s.*)", "$1's$2");
					System.out.println("New normalized name is " + normalizedName);
				}

				if (!curCN.getName().equals(normalizedName)) {
					curCN.setName(normalizedName);
					writeback = true;
				}
			} catch (Exception e) {
				System.out.println("Exception working on taxon " + node.getId());
				e.printStackTrace();
				System.exit(0);
			}
		}
		if (writeback) {
			System.out.println("Changed " + node.getId());
			writebackTaxon(node, vfs);
		}
	}

	public static void fixTaxonomicAuthority(VFS vfs, String rootPath) throws NotFoundException, Exception {
		registerDatasource("rldb", "jdbc:access:////usr/data/rldbRelationshipFree.mdb",
				"com.hxtt.sql.access.AccessDriver", "", "");
		switchToDBSession("rldb");

		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL))
				fixTaxonomicAuthority(vfs, curURL + "/");
			else
				doFixTaxonomicAuthority(vfs, curURL);

			if (taxaCount % 1000 == 0)
				System.out.println("Through " + taxaCount);
		}
	}

	public static void killAllButBeetles(VFS vfs, String rootPath) throws Exception {
		if (footprint == null)
			footprint = Arrays
					.asList(new String[] { "ANIMALIA", "ARTHROPODA", "INSECTA", "COLEOPTERA", "SCARABAEIDAE" });

		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL)) {
				killAllButBeetles(vfs, curURL + "/");
				if (vfs.list(curURL).length == 0)
					vfs.delete(curURL);
			} else
				doKillAllButBeetles(vfs, curURL);

			if (taxaCount % 1000 == 0)
				System.out.println("Through " + taxaCount);
		}
	}

	public static void main(final String args[]) {

	}

	public static void normalizeCommonNames(VFS vfs, String rootPath) throws NotFoundException {
		for (String url : vfs.list(rootPath)) {
			String curURL = rootPath + url;

			if (vfs.isCollection(curURL))
				normalizeCommonNames(vfs, curURL + "/");
			else
				doNormalize(vfs, curURL);
		}
	}

	private static String normalizeName(String unnormalized) {
		if (unnormalized.equals(""))
			return null;

		if (unnormalized.indexOf(" ") == -1)
			return Character.toUpperCase(unnormalized.charAt(0)) + unnormalized.substring(1).toLowerCase();

		String[] split = unnormalized.split(" ");
		StringBuilder ret = new StringBuilder();

		for (String curSplit : split)
			if (curSplit.length() > 0)
				ret.append(Character.toUpperCase(curSplit.charAt(0)) + curSplit.substring(1).toLowerCase() + " ");

		if (ret.indexOf("'") > 0 && (ret.charAt(ret.indexOf("'") + 1) + "").matches("[a-zA-Z]"))
			ret.replace(ret.indexOf("'") + 1, ret.indexOf("'") + 2, Character.toUpperCase(ret
					.charAt(ret.indexOf("'") + 1))
					+ "");

		return ret.toString().trim();
	}

	private static void registerDatasource(String sessionName, String URL, String driver, String username, String pass)
			throws Exception {
		DBSessionFactory.registerDataSource(sessionName, URL, driver, username, pass);
	}

	private static void switchToDBSession(String sessionName) throws Exception {
		ec = new SystemExecutionContext(sessionName);
		ec.setExecutionLevel(ExecutionContext.READ_WRITE);
	}

	private static boolean writebackTaxon(TaxonNode node, VFS vfs) {
		String url = ServerPaths.getURLForTaxa(node.getId() + "");
		return DocumentUtils.writeVFSFile(url, vfs, TaxonNodeFactory.nodeToDetailedXML(node));
	}
}
