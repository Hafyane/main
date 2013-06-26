package org.lareferencia.backend.rest;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.lareferencia.backend.domain.NationalNetwork;
import org.lareferencia.backend.domain.NetworkSnapshot;
import org.lareferencia.backend.domain.OAIProviderStat;
import org.lareferencia.backend.domain.OAIRecord;
import org.lareferencia.backend.domain.RecordStatus;
import org.lareferencia.backend.domain.SnapshotStatus;
import org.lareferencia.backend.harvester.OAIRecordMetadata;
import org.lareferencia.backend.harvester.OAIRecordMetadata.OAIRecordMetadataParseException;
import org.lareferencia.backend.indexer.IIndexer;
import org.lareferencia.backend.repositories.NationalNetworkRepository;
import org.lareferencia.backend.repositories.NetworkSnapshotRepository;
import org.lareferencia.backend.repositories.OAIProviderStatRepository;
import org.lareferencia.backend.repositories.OAIRecordRepository;
import org.lareferencia.backend.tasks.SnapshotManager;
import org.lareferencia.backend.util.JsonDateSerializer;
import org.lareferencia.backend.validator.IValidator;
import org.lareferencia.backend.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Handles requests for the application home page.
 */
@Controller
public class BackEndController {
	
	
	private static final int STATS_PAGE_SIZE = 100;

	@Autowired 
	private ApplicationContext applicationContext;
	
	@Autowired
	private NationalNetworkRepository nationalNetworkRepository;
	
	@Autowired
	private NetworkSnapshotRepository networkSnapshotRepository;
	
	@Autowired
	private OAIRecordRepository recordRepository;
	
	@Autowired 
	private OAIProviderStatRepository oaiProviderStatRepository;
	
	@Autowired
	IIndexer indexer;
	
	@Autowired
	IValidator validator;

	
	//private static final Logger logger = LoggerFactory.getLogger(BackEndController.class);
	
	//private static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		//logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "";
	}
	
	
	/************************** Backend ************************************/
	
	@RequestMapping(value="/private/harvester/{networkID}", method=RequestMethod.GET)
	public ResponseEntity<String> harvesting(@PathVariable Long networkID) {
		//TODO: debiera chequear la existencia de la red
		
		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
		manager.lauchHarvesting(networkID);
		
		return new ResponseEntity<String>("Havesting:" + networkID, HttpStatus.OK);
	}
	
	/**
	 * Este servicio para cada origen explora los sets (no los almacenados sino los provistos por ListSets)
	 * y para cada uno de ellos realiza una cosecha. Si los sets son disjuntos la coschecha final es completa y
	 * sin repeticiones
	 * @param networkID
	 * @return
	 */
	@RequestMapping(value="/private/harvestSetBySet/{networkID}", method=RequestMethod.GET)
	public ResponseEntity<String> harvestSetBySet(@PathVariable Long networkID) {
		
		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
		manager.lauchSetBySetHarvesting(networkID);
		
		return new ResponseEntity<String>("Havesting:" + networkID, HttpStatus.OK);
	}
	
	@RequestMapping(value="/private/resumeHarvestingBySnapshotID/{snapshotID}", method=RequestMethod.GET)
	public ResponseEntity<String> resumeHarvestingBySnapshotID(@PathVariable Long snapshotID) {
		
		SnapshotManager manager = applicationContext.getBean("snapshotManager", SnapshotManager.class);
		manager.relauchHarvesting(snapshotID);
		
		return new ResponseEntity<String>("Relauch Havesting:" + snapshotID, HttpStatus.OK);
	}
	
	
	
	
	@Transactional
	@RequestMapping(value="/private/deleteAllButLGKSnapshot/{id}", method=RequestMethod.GET)
	public ResponseEntity<String> deleteAllButLGKSnapshot(@PathVariable Long id) throws Exception {
		
		NationalNetwork network = nationalNetworkRepository.findOne(id);
		if ( network == null )
			throw new Exception("No se encontró RED");
		
		
		NetworkSnapshot lgkSnapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(id);
		
		if ( lgkSnapshot != null) {
		
			for ( NetworkSnapshot snapshot:network.getSnapshots() ) {
				if ( !snapshot.getId().equals(lgkSnapshot.getId()) && !snapshot.isDeleted() ) {
					// borra los registros
					recordRepository.deleteBySnapshotID(snapshot.getId());
					// lo marca borrado
					snapshot.setDeleted(true);
					// almacena el estado del snap
					networkSnapshotRepository.save(snapshot);
				}
			}
		}
		else
			throw new Exception("No se encontró LGK Snapshot para esa red, no se realizaron cambios");

		
		return new ResponseEntity<String>("Borrados snapshots excedentes de:" + network.getName(), HttpStatus.OK);
	}
	
	@Transactional
	@RequestMapping(value="/private/deleteRecordsBySnapshotID/{id}", method=RequestMethod.GET)
	public ResponseEntity<Map<String,String>> deleteRecordsBySnapshotID(@PathVariable Long id) {
		
		recordRepository.deleteBySnapshotID(id);
		
		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
		snapshot.setDeleted(true);
		networkSnapshotRepository.save(snapshot);
		
		
		Map<String,String> result = new HashMap<String, String>();
		result.put("result", "OK");
		
		ResponseEntity<Map<String,String>> response = new ResponseEntity<Map<String,String>>(result, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value="/private/indexValidRecordsBySnapshotID/{id}", method=RequestMethod.GET)
	public ResponseEntity<Map<String,String>> indexRecordsBySnapshotID(@PathVariable Long id) {
		
		Map<String,String> result = new HashMap<String, String>();
		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
	
		if ( indexer.index(snapshot) ) {
			result.put("result", "OK");
		}
		else {
			result.put("result", "ERROR");
		}	
		
		System.gc();
		ResponseEntity<Map<String,String>> response = new ResponseEntity<Map<String,String>>(result, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value="/private/validateRecordByID/{id}", method=RequestMethod.GET)
	public ResponseEntity<ValidationResult> validateRecordByID(@PathVariable Long id) throws OAIRecordMetadataParseException {
		
		
		OAIRecord record = recordRepository.findOne( id );	
		OAIRecordMetadata metadata = new OAIRecordMetadata(record.getIdentifier(), record.getOriginalXML());
		ValidationResult result = validator.validate(metadata);
		
		ResponseEntity<ValidationResult> response = new ResponseEntity<ValidationResult>(result, HttpStatus.OK);
		
		return response;
	}

	/**************************** FrontEnd ************************************/
	
	@RequestMapping(value="/public/lastGoodKnowSnapshotByNetworkID/{id}", method=RequestMethod.GET)
	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(@PathVariable Long id) {
			
		NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(id);
		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
			snapshot,
			snapshot == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
		);
		return response;
	}
	
	@RequestMapping(value="/public/lastGoodKnowSnapshotByCountryISO/{iso}", method=RequestMethod.GET)
	public ResponseEntity<NetworkSnapshot> getLGKSnapshot(@PathVariable String iso) throws Exception {
		
		NationalNetwork network = nationalNetworkRepository.findByCountryISO(iso);
		if ( network == null ) // TODO: Implementar Exc
			throw new Exception("No se encontró RED perteneciente a: " + iso);
		
		NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(network.getId());
		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot válido de la RED: " + iso);
		
		ResponseEntity<NetworkSnapshot> response = new ResponseEntity<NetworkSnapshot>(
			snapshot,
			snapshot == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
		);
		return response;
	}
	
	@RequestMapping(value="/public/listSnapshotsByCountryISO/{iso}", method=RequestMethod.GET)
	public ResponseEntity<List<NetworkSnapshot>> listSnapshotsByCountryISO(@PathVariable String iso) throws Exception {
		
		NationalNetwork network = nationalNetworkRepository.findByCountryISO(iso);
		if ( network == null )
			throw new Exception("No se encontró RED perteneciente a: " + iso);
		
		ResponseEntity<List<NetworkSnapshot>> response = new ResponseEntity<List<NetworkSnapshot>>(networkSnapshotRepository.findByNetworkOrderByEndTimeAsc(network), HttpStatus.OK);
		
		return response;
	}
	
	
	@RequestMapping(value="/public/listNetworks", method=RequestMethod.GET)
	public ResponseEntity<List<NetworkInfo>> listNetworks() {
		
				
		List<NationalNetwork> allNetworks = nationalNetworkRepository.findByPublishedOrderByNameAsc(true);//OrderByName();
		List<NetworkInfo> NInfoList = new ArrayList<NetworkInfo>();

		for (NationalNetwork network:allNetworks) {
			
			NetworkInfo ninfo = new NetworkInfo();
			ninfo.networkID = network.getId();
			ninfo.country = network.getCountryISO();
			ninfo.name = network.getName();
			
			NetworkSnapshot snapshot = networkSnapshotRepository.findLastGoodKnowByNetworkID(network.getId());
			
			if ( snapshot != null) {
				
				ninfo.snapshotID = snapshot.getId();
				ninfo.datestamp = snapshot.getEndTime();
				ninfo.size = snapshot.getSize();
				ninfo.validSize = snapshot.getValidSize();
				
			}		
			NInfoList.add( ninfo );		
		}
	
		ResponseEntity<List<NetworkInfo>> response = new ResponseEntity<List<NetworkInfo>>(NInfoList, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value="/public/listNetworksHistory", method=RequestMethod.GET)
	public ResponseEntity<List<NetworkHistory>> listNetworksHistory() {
		
		List<NationalNetwork> allNetworks = nationalNetworkRepository.findByPublishedOrderByNameAsc(true);//OrderByName();
		List<NetworkHistory> NHistoryList = new ArrayList<NetworkHistory>();

		for (NationalNetwork network:allNetworks) {	
			NetworkHistory nhistory = new NetworkHistory();
			nhistory.networkID = network.getId();
			nhistory.country = network.getCountryISO();
			nhistory.validSnapshots =  networkSnapshotRepository.findByNetworkAndStatusOrderByEndTimeAsc(network, SnapshotStatus.VALID);
			NHistoryList.add( nhistory );		
		}
	
		ResponseEntity<List<NetworkHistory>> response = new ResponseEntity<List<NetworkHistory>>(NHistoryList, HttpStatus.OK);
		
		return response;
	}
	
	@RequestMapping(value="/public/listProviderStats", method=RequestMethod.GET)
	@ResponseBody
	public PageResource<OAIProviderStat> listProviderStats(@RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) {
		
		if (page == null)
			page = 0;
		if (size == null)
			size = 100;
		
		Page<OAIProviderStat> pageResult = oaiProviderStatRepository.findAll( new PageRequest(page, size, new Sort(Sort.Direction.DESC,"requestCount")));	
		
		return new PageResource<OAIProviderStat>(pageResult,"page","size");
	}
	
	@RequestMapping(value="/public/listInvalidRecordsInfoBySnapshotID/{id}", method=RequestMethod.GET)
	@ResponseBody
	public PageResource<OAIRecord> listInvalidRecordsInfoBySnapshotID(@PathVariable Long id, @RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) throws Exception {
		
		NetworkSnapshot snapshot = networkSnapshotRepository.findOne(id);
		
		if (snapshot == null) // TODO: Implementar Exc
			throw new Exception("No se encontró snapshot con id: " + id);
			
		if (page == null)
			page = 0;
		if (size == null)
			size = 100;
		
		Page<OAIRecord> pageResult = recordRepository.findBySnapshotAndStatus(snapshot, RecordStatus.INVALID, new PageRequest(page, size));	
		
		return new PageResource<OAIRecord>(pageResult,"page","size");
	}
	
	
	/**************  Clases de retorno de resultados *******************/
	
	@Getter
	@Setter
	class NetworkInfo {	
		private Long   networkID;
		private String country;
		private String name;
		
		private Long snapshotID;
		
		@JsonSerialize(using=JsonDateSerializer.class)
		private Date datestamp;
		private int size;
		private int validSize;
	}
	
	@Getter
	@Setter
	class NetworkHistory {	
		private Long   networkID;
		private String country;
		private List<NetworkSnapshot> validSnapshots;
	}
	
	@Getter
	@Setter
	class OAIRecordValidationInfo {	
		private Long   id;
		private String originalHeaderId;
		private boolean isValid;
		private boolean isDriverType;
		private String  dcTypeFieldContents;
	}
	
	
	
}