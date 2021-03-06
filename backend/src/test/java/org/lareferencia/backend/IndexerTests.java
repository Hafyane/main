/*******************************************************************************
 * Copyright (c) 2013 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Lautaro Matas (lmatas@gmail.com) - Desarrollo e implementación
 *     Emiliano Marmonti(emarmonti@gmail.com) - Coordinación del componente III
 * 
 * Este software fue desarrollado en el marco de la consultoría "Desarrollo e implementación de las soluciones - Prueba piloto del Componente III -Desarrollador para las herramientas de back-end" del proyecto “Estrategia Regional y Marco de Interoperabilidad y Gestión para una Red Federada Latinoamericana de Repositorios Institucionales de Documentación Científica” financiado por Banco Interamericano de Desarrollo (BID) y ejecutado por la Cooperación Latino Americana de Redes Avanzadas, CLARA.
 ******************************************************************************/
package org.lareferencia.backend;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lareferencia.backend.indexer.IIndexer;
import org.lareferencia.backend.repositories.NetworkSnapshotRepository;
import org.lareferencia.backend.transformer.ITransformer;
import org.lareferencia.backend.validator.IValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class IndexerTests {
	
	static String xmlstring = "<metadata xmlns=\"http://www.openarchives.org/OAI/2.0/\">" +
			"<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">" +
			"<dc:title>Instrumentación y control de un secador de fruta tipo túnel</dc:title>" +
			"<dc:creator>Elorza R., Jorge Rafael</dc:creator>" +
			"<dc:creator>Sandoval González, José</dc:creator>" +
			"<dc:creator>Martínez G, Martín T.</dc:creator>" +
			"<dc:contributor>Lautaro Matas</dc:contributor>" +
			"<dc:date>2002-01-01</dc:date>" +
			"<dc:description>En este trabajo se proporciona un ejemplo con cierto detalle de cómo emplear una PC y una tarjeta</dc:description>" +
			"<dc:source>instname:Universidad Nacional Autónoma de Nuevo León (UANL)</dc:source>" +
			"<dc:publisher>Universidad Nacional Autónoma de Nuevo León (UANL)</dc:publisher>" +
			"<dc:source>reponame:Red Mexicana de Repositorios Institucionales</dc:source>" +
			"<dc:type>Artículo</dc:type>" +
			"<dc:identifier>http://eprints.uanl.mx/504/</dc:identifier>" +
			"<dc:rights>Acceso Abierto</dc:rights>" +
			"<dc:rights>info:eu-repo/semantics/openAccess</dc:rights>" +
			"<dc:language>spa</dc:language>" +
			"<dc:subject>TA Engineering (General). Civil engineering (General)</dc:subject>" +
			"<dc:format>application/pdf</dc:format>" +
			"<dc:identifier>http://eprints.uanl.mx/504/1/instrumentacion_ctrl.pdf</dc:identifier>" +
			"<dc:identifier>Elorza R., Jorge Rafael y Sandoval González, José y Martínez G, Martín T. (2002) Instrumentación y control de un secador de fruta tipo túnel. Ciencia UANL, 5 (4). ISSN 1405-9177</dc:identifier>" +
			"<dc:type>info:eu-repo/semantics/article</dc:type>" +
			"<dc:type>Artículo</dc:type>" +
			"<dc:type>info:eu-repo/semantics/acceptedVersion</dc:type>" +
			"</oai_dc:dc>" +
			"</metadata>";

	@Autowired
	IValidator validator;
	
	@Autowired
	ITransformer transformer;
	
	@Autowired
	IIndexer indexer;
	
	@Autowired
	@Qualifier(value="indexerIntelligo")
	IIndexer indexerIntelligo;

	@Autowired
	NetworkSnapshotRepository networkSnapshotRepository;
/*
	
	@Test
	public void testRecordWiredDriverTransformer() throws Exception {
		
		OAIRecord orecord = new OAIRecord("dummy", xmlstring);
		
		orecord.setPublishedXML( orecord.getOriginalXML() );

		NationalNetwork network = new NationalNetwork();
		
		network.setCountryISO("MX");
		network.setName("Mexico");
		
		NetworkSnapshot snapshot = new NetworkSnapshot();
		orecord.setSnapshot(snapshot);
		
		System.out.println( xmlstring );
		
		//Document doc = indexer.transform(orecord, network);
		
		//System.out.println(indexer.transform(orecord, network)  );
		
	}
	
	@Test
	public void testSolrIndexer() throws Exception {
		
		
		indexer.index(networkSnapshotRepository.findOne(7L));
		
		
	}
	
*/
	@Test
	public void testIntelligoIndexer() throws Exception {
		
		
		indexerIntelligo.index(networkSnapshotRepository.findOne(2L));
		
		
	}
	
	
	
}
