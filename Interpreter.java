package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.parsers.*;

public class Interpreter {

	/**
	 * @param args
	 */

	public static String context; //= "/atomcat6temps/webapps/ontovis/";
	//"Archivos de programa/Apache Software Foundation/Tomcat 6.0/webapps/ontovis/";
	
	public static String session = "";

	public static void main(String ontology1, String ontology2,
			String alignment, String mode, String sid, String path) {
		
		context = path;

		session = sid;

		if (mode.equalsIgnoreCase("tree")) {
			treemode(ontology1, ontology2, alignment);
		} else if (mode.equalsIgnoreCase("table")) {
			tablemode(ontology1, ontology2, alignment);
		} else
			System.out
					.println("arguments: file1.owl, file2.owl, matching.rdf, tree|table");
		return;

	}

	private static void keySorter(BufferedWriter output,
			LinkedHashSet<String> todoslosnodos,
			LinkedHashMap<String, Integer> niveles,
			LinkedHashMap<String, String> padres) {
		LinkedHashSet<String> copiaClaves = new LinkedHashSet<String>();
		LinkedHashSet<String> reservaClaves = new LinkedHashSet<String>();
		copiaClaves.addAll(padres.keySet());
		todoslosnodos.add("Thing");
		niveles.put("Thing", -1);
		keySubSorter(output, todoslosnodos, niveles, 0, "Thing", padres,
				copiaClaves, reservaClaves);
		niveles.remove("Thing");
		todoslosnodos.remove("Thing");
	}

	private static void keySubSorter(BufferedWriter output,
			LinkedHashSet<String> todoslosnodos,
			LinkedHashMap<String, Integer> niveles, int nivel, String padre,
			LinkedHashMap<String, String> padres, LinkedHashSet<String> claves,
			LinkedHashSet<String> reserva) {
		if (claves.isEmpty()) {
			if (reserva.isEmpty()) {
				//System.out.println("Finish: all empty");
				return;
			} else {
				claves.clear();
				claves.addAll(reserva);
				reserva.clear();
				//System.out.println("Buffer dumped, "+claves.size()+" keys remaining");
				return;
			}
		}
		Iterator<String> it = claves.iterator();
		if (it.hasNext()) {
			String hijo = it.next();
			it = null;
			if (padres.get(hijo).equalsIgnoreCase(padre)) {

				// RAMA A: AÑADIR NODO
				todoslosnodos.add(hijo);
				niveles.put(hijo, nivel);
				//System.out.println("Added "+hijo+" whose father is "+padre);
				// output.write("nodo(con," + (nivel) + ",\"" + hijo +
				// "\",0);\n");
				claves.remove(hijo); claves.addAll(reserva); reserva.clear();
				//System.out.println("Searching nodes whose father is "+hijo);
				keySubSorter(output, todoslosnodos, niveles, (nivel + 1),
						hijo, padres, claves, reserva);
				if (!claves.isEmpty()){
					//System.out.println("Backtracking to father "+padre);
					keySubSorter(output, todoslosnodos, niveles, nivel,
							padre, padres, claves, reserva);
				}
			} else {

				// RAMA B: RESERVA
				reserva.add(hijo);
				claves.remove(hijo);
				//System.out.println("Skipped "+hijo+" whose father is "+padres.get(hijo)+" instead of "+padre);
				keySubSorter(output, todoslosnodos, niveles, nivel, padre,
						padres, claves, reserva);
				return;
			}
		}
		return;
	}

	private static void treemode(String ontology1, String ontology2,
			String alignment) {

		try {
			FileWriter fstream = new FileWriter(context + "/" + session + ".tmp");
			BufferedWriter output = new BufferedWriter(fstream);

			/*
			 * Cabecera del fichero final
			 */

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document onto1 = docBuilder.parse(new File(ontology1));
			Document onto2 = docBuilder.parse(new File(ontology2));
			//Document onto1 = docBuilder.parse(new File(context + "temps/" + ontology1));
			//Document onto2 = docBuilder.parse(new File(context + "temps/" + ontology2));

			NodeList pairs1;
			NodeList pairs2;
			LinkedHashSet<String> ancestors1 = new LinkedHashSet<String>();
			LinkedHashSet<String> ancestors2 = new LinkedHashSet<String>();
			LinkedHashSet<String> allClassNodes = new LinkedHashSet<String>();
			LinkedHashSet<String> propertyNodes = new LinkedHashSet<String>();
			LinkedHashSet<String> datatypeNodes = new LinkedHashSet<String>();
			LinkedHashMap<String, Integer> levels = new LinkedHashMap<String, Integer>();

			/*
			 * Parametrización de la maquetación en función de la colección
			 */

			/*
			 * Función de dibujo y funciones auxiliares de dibujo
			 */
			// salida.write(fragmentos.tree1);

			
			boolean labelsFound = true;
			NodeList classes = onto1.getElementsByTagName("rdfs:label");
			NodeList properties = onto1.getElementsByTagName("owl:ObjectProperty");
			NodeList datatypes  = onto1.getElementsByTagName("owl:DatatypeProperty");
			
			if (classes.getLength() == 0) {
				labelsFound = false;
				classes = onto1.getElementsByTagName("owl:Class");
			}
			
			int s = 0;
			NodeList superclasses = onto1.getElementsByTagName("rdfs:subClassOf");
			LinkedHashMap<String, String> lostNodes = new LinkedHashMap<String, String>();
			LinkedHashMap<String, String> nodeLabels = new LinkedHashMap<String, String>();
			

			//String lastParent = "";
			String parent = "";
			String child = "";
			
			for (int i = 0; i < 2; i++) {
			
			if (labelsFound) {
				
				//Las etiquetas contienen a todas las clases y propiedades
				
				for (s = 0; s < classes.getLength(); s++) {
					Node clase = classes.item(s);
					String atributo = "";
					if ((clase.getParentNode().getNodeName()
							.equalsIgnoreCase("owl:Class"))) {

						if (clase.getParentNode().hasAttributes()) {

							NamedNodeMap atributos = clase.getParentNode()
									.getAttributes();
							
							if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:about")
									|| atributos.item(0).getNodeName()
											.equalsIgnoreCase("rdf:ID"))
								
								atributo = atributos.item(0).getNodeValue();
							
							if (atributo.contains("#"))
								atributo = atributo.split("#")[1];
						}
						
						//Todos los nodos de clase se incluyen como hijo de Thing
						
						if (!atributo.isEmpty()) {
							lostNodes.put(atributo, "Thing");
							nodeLabels.put(atributo, classes.item(s).getTextContent());
						}

					} else if ((clase.getParentNode().getNodeName()
							.equalsIgnoreCase("owl:DatatypeProperty"))
							|| (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:ObjectProperty"))) {
						
						if (clase.getParentNode().hasAttributes()) {

							NamedNodeMap atributos = clase.getParentNode()
									.getAttributes();
							
							if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:about")
									|| atributos.item(0).getNodeName()
											.equalsIgnoreCase("rdf:ID"))
								
								atributo = atributos.item(0).getNodeValue();
							
							if (atributo.contains("#"))
								atributo = atributo.split("#")[1];
						}

						//Las propiedades van a una colección aparte que no se ordena
						
						if (!atributo.isEmpty()) {
							if (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:DatatypeProperty"))
								datatypeNodes.add(atributo);
							else if (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:ObjectProperty"))
								propertyNodes.add(atributo);
							nodeLabels.put(atributo, classes.item(s).getTextContent());
						}
						
					}
				}
				
			}else{
			
				for (s = 0; s < (classes.getLength()+properties.getLength()+datatypes.getLength()); s++) {
					
					Node clase;
					
					if (s < classes.getLength()) {
						clase = classes.item(s);
					} else if(classes.getLength() <= s && s < (classes.getLength()+properties.getLength())){
						clase = properties.item(s - (classes.getLength()));
					} else {
						clase = datatypes.item(s - (classes.getLength()+properties.getLength()));
					}
					
					String atributo = "";
					
					if (clase.hasAttributes()) {
						NamedNodeMap atributos = clase.getAttributes();
						if (atributos.item(0).getNodeName()
								.equalsIgnoreCase("rdf:ID")) {
							atributo = atributos.item(0).getNodeValue();

							
							if (s < classes.getLength()) {
								lostNodes.put(atributo, "Thing");
								nodeLabels.put(atributo, "$");
							} else if (s < properties.getLength()){
								propertyNodes.add(atributo);
								nodeLabels.put(atributo, "$");
							} else {
								datatypeNodes.add(atributo);
								nodeLabels.put(atributo, "$");
							}
							
							
						} else if (atributos.item(0).getNodeName()
								.equalsIgnoreCase("rdf:about")) {
							Node padre = clase.getParentNode();
							if (padre != null
									&& (padre.getNodeName().equalsIgnoreCase("owl:Ontology")
									 || padre.getNodeName().equalsIgnoreCase("rdfs:subClassOf")
							         || padre.getNodeName().equalsIgnoreCase("rdf:RDF"))) {
								atributo = atributos.item(0).getNodeValue();
								if (atributo.contains("#")) {
									atributo = atributo.split("#")[1];
									
									if (s < classes.getLength()) {
										lostNodes.put(atributo, "Thing");
										nodeLabels.put(atributo, "$");
									} else if (s < properties.getLength()){
										propertyNodes.add(atributo);
										nodeLabels.put(atributo, "$");
									} else {
										datatypeNodes.add(atributo);
										nodeLabels.put(atributo, "$");
									}
									
								}
							}
						}
					}
				}
				
			}
			
			for (s = 0; s < superclasses.getLength(); s++) {
				Node clase = superclasses.item(s);
				NamedNodeMap atributos = clase.getAttributes();

				if ((atributos.getLength() >= 1)
						&& (atributos.item(0).getNodeName()
								.equalsIgnoreCase("rdf:resource"))) {
					parent = atributos.item(0).getNodeValue();
					if (parent.contains("http:")) {
						parent = parent.split("/")[parent.split("/").length - 1];
					}
					if (parent.contains("#")) {
						parent = parent.split("#")[parent.split("#").length - 1];
					}
				} else
					parent = "";

				child = superclasses.item(s).getParentNode()
						.getAttributes().item(0).getNodeValue();
				
				if (child.contains("#"))
					child = child.split("#")[1];
				
				if (!parent.isEmpty())
					lostNodes.put(child, parent);
			}
			

				/*
				 * CODIGO ANTERIOR DE TREE SOLO PARA REFERENCIA
				 * Procesar nodo raíz
				 
				
				while (keepGoing && s < classes.getLength()) {
					Node clase = classes.item(s);
					
						NamedNodeMap attributes = clase.getAttributes();
						if (attributes.getLength() > 0) {
						if ((attributes.item(0).getNodeName()
								.equalsIgnoreCase("rdf:ID"))) {
							
							if (hayEtiquetas) {
								ultpadre = clase.getChildNodes().item(1)
										.getTextContent();
							} else
								ultpadre = atributos.item(0).getNodeValue();
							
							mode = 0;
							keepGoing = false;
						} else if (attributes.item(0).getNodeName()
								.equalsIgnoreCase("rdf:about")
								&& clase.getParentNode().getNodeName()
										.equalsIgnoreCase("rdfs:subClassOf")) {
							
							ultpadre = atributos.item(0).getNodeValue()
									.split("#")[1];
							
							mode = 1;
							keepGoing = false;
						} else {
							s++;
						}
						} else {
							s++;
						}
					
				}
				
				
				if (mode == 0) {

					for (int k = s; k < classes.getLength(); k++) {
						Node clase = classes.item(k);
						
						NamedNodeMap atributos = clase.getAttributes();
						if ((atributos.getLength() > 0)
								&& (atributos.item(0).getNodeName()
										.equalsIgnoreCase("rdf:ID"))) {
							if (labelsFound) {
								child = atributos.item(0).getNodeValue();
								childLabel = clase.getChildNodes().item(1)
										.getTextContent();
								if (!lostNodes.containsKey(child)){
									lostNodes.put(child, "Thing");
									nodeLabels.put(child, childLabel);
								}
							} else {
								child = atributos.item(0).getNodeValue();
								if (!lostNodes.containsKey(child))
									lostNodes.put(child, "Thing");
							}
						}
					}
					
					for (s = 0; s < superclasses.getLength(); s++) {

						Node clase = superclasses.item(s);
						NamedNodeMap atributos = clase.getAttributes();

						if ((atributos.getLength() >= 1)
								&& (atributos.item(0).getNodeName()
										.equalsIgnoreCase("rdf:resource"))) {
							parent = atributos.item(0).getNodeValue();
							if (parent.contains("http:")) {
								parent = parent.split("/")[parent.split("/").length - 1];
							} else if (parent.contains("#")) {
								parent = parent.split("#")[parent.split("#").length - 1];
							}
						} else
							parent = "";

						child = superclasses.item(s).getParentNode()
								.getAttributes().item(0).getNodeValue();
						
						if (labelsFound){
							childLabel = superclasses.item(s).getParentNode().getChildNodes().item(3).getTextContent();
							
							nodeLabels.put(child, childLabel);
							for (int f=0; f<subclases.item(s).getParentNode().getChildNodes().getLength(); f++){
								System.out.println("Hijo "+f+": "+subclases.item(s).getParentNode().getChildNodes().item(f).getNodeName()
										+subclases.item(s).getParentNode().getChildNodes().item(f).getTextContent());
								}
							System.out.println("Hijo en el paso "+s+ ": "+hijo);
						}
						
						if (!parent.isEmpty()) {
							lostNodes.put(child, parent);
						}
					}
				} else if (mode == 1) {
					for (s = 0; s < classes.getLength(); s++) {

						Node clase = classes.item(s);
						NamedNodeMap atributos = clase.getAttributes();
						//
						parent = "";
						
						if ((atributos.getLength() >= 1)
								&& (atributos.item(0).getNodeName()
										.equalsIgnoreCase("rdf:about"))) {
							if (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:Ontology")) {
								child = atributos.item(0).getNodeValue();
								if (child.contains("#")) {
									child = child.split("#")[child.split("#").length - 1];
								}
								if (!lostNodes.containsKey(child)) {
									lostNodes.put(child, "Thing");
									
								}
							} else if (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("rdfs:subClassOf")) {
								parent = atributos.item(0).getNodeValue();
								if (parent.contains("#")) {
									parent = parent.split("#")[parent
											.split("#").length - 1];
								}
								if (!lostNodes.containsKey(parent)) {
									lostNodes.put(parent, "Thing");
								}
							}
						}

						if (!parent.isEmpty()) {
							
							child = classes.item(s).getParentNode()
									.getParentNode().getAttributes()
									.item(0).getNodeValue().split("#")[1];
							
							if (labelsFound) {
								childLabel = classes.item(s).getParentNode()
										.getParentNode().getChildNodes().item(1).getTextContent();
								nodeLabels.put(child, childLabel);
								//System.out.println("Hijo en el paso "+s+ ": "+hijo);
							}
							
							lostNodes.put(child, parent);
						} 
						else{
							if (labelsFound) {
								child = classes.item(s).getChildNodes().item(1).getTextContent();
								//System.out.println("Hijo en el paso "+s+ ": "+hijo);
							}
							if (!labelsFound || child.isEmpty()) {
								child = classes.item(s).getAttributes()
										.item(0).getNodeValue().split("#")[1];
							}
							lostNodes.put(child, "Thing");
						}
					}
				}*/

					keySorter(output, allClassNodes, levels, lostNodes);
					
					//keySorter(output, allClassNodes, levels, base, parent, lostNodes);
					
				// preparar segunda columna
				if (i == 0) {
					//System.out.println("Out of first KeySorter");
					// salida.write("var nombresA = "+todoslosnodos.toString().replace(",",
					// "\",\"").replace("[", "[\"").replace("]", "\"]")+";\n");
					// salida.write("var nivelesA = "+niveles.values().toString()+";\n");
					output.write("classIDsA = "
							+ allClassNodes.toString().replace(", ", "\",\"")
									.replace("[", "[\"").replace("]", "\"]")
							+ ";\n");
					output.write("levelsA = " + levels.values().toString()
							+ ";\n");
					
					output.write("classesA = [");
					
					Iterator<String> it = allClassNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					output.write("0];\n");
					
					output.write("propertiesA = "
							+ propertyNodes.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]")
					+ ";\n");
					output.write("datatypesA = "
							+ datatypeNodes.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]")
					+ ";\n");
					output.write("propertyLabelsA = [");
					it = propertyNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					output.flush();
					output.write("0];\n");
					output.write("datatypeLabelsA = [");
					it = datatypeNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					output.flush();
					output.write("0];\n");
					// salida.write(fragmentosKinetic.tree1);

					/*
					 * salida.write("linea=0\n");
					 * salida.write("prevnivel=30\n");
					 */

					classes = onto2.getElementsByTagName("rdfs:label");
					
					if (classes.getLength() <= 0) {
						labelsFound = false;
						classes = onto2.getElementsByTagName("owl:Class");
					} else labelsFound = true;
					
					properties = onto2.getElementsByTagName("owl:ObjectProperty");
					datatypes  = onto2.getElementsByTagName("owl:DatatypeProperty");
					superclasses = onto2.getElementsByTagName("rdfs:subClassOf");

					// agregar ultimo elemento de ontologia 1
					ancestors1.addAll(allClassNodes);
					ancestors1.addAll(propertyNodes);
					ancestors1.addAll(datatypeNodes);

					// seleccionar estructura de memoria 2
					lostNodes.clear();
					nodeLabels.clear();
					allClassNodes.clear();
					propertyNodes.clear();
					datatypeNodes.clear();
					
					child = "";
					levels.clear();
				}

				if (i == 1) {
					//System.out.println("Out of second KeySorter");
					// salida.write("var nombresB = "+todoslosnodos.toString().replace(", ",
					// "\", \"").replace("[", "[\"").replace("]", "\"]")+";\n");
					// salida.write("var nivelesB = "+niveles.values().toString()+";\n");
					output.write("classIDsB = "
							+ allClassNodes.toString().replace(", ", "\",\"")
									.replace("[", "[\"").replace("]", "\"]")
							+ ";\n");
					output.write("levelsB = " + levels.values().toString()
							+ ";\n");
					
					output.write("classesB = [");
					
					Iterator<String> it = allClassNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					output.flush();
					output.write("0];\n");
					
					output.write("propertiesB = "
							+ propertyNodes.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]")
					+ ";\n");
					
					output.write("datatypesB = "
							+ datatypeNodes.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]")
					+ ";\n");
					
					output.write("propertyLabelsB = [");
					it = propertyNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					
					output.write("0];\n");
					output.write("datatypeLabelsB = [");
					it = datatypeNodes.iterator();
					while(it.hasNext()){
						output.write("\""+nodeLabels.get(it.next())+"\",");
					}
					
					output.write("0];\n");

					// agregar ultimo elemento de ontologia 2
					ancestors2.addAll(allClassNodes);
					ancestors2.addAll(propertyNodes);
					ancestors2.addAll(datatypeNodes);
					lostNodes.clear();
					nodeLabels.clear();

					allClassNodes.clear();
					propertyNodes.clear();
				}

			}

			Runtime r = Runtime.getRuntime();
			r.gc();

			Document matching = docBuilder.parse(new File(alignment));
			//Document matching = docBuilder.parse(new File(context + "temps/" + alignment));
			pairs1 = matching.getElementsByTagName("entity1");
			pairs2 = matching.getElementsByTagName("entity2");
			NodeList valores = matching.getElementsByTagName("measure");
			NodeList signos = matching.getElementsByTagName("relation");

			// salida.write("var uniones = [");
			output.write("uniones = [");

			for (int i1 = 0; i1 < pairs1.getLength(); i1++) {
				boolean completo = false;
				int indice1 = 0, indice2 = 0;
				Node celda = pairs1.item(i1);

				Iterator<String> it1 = ancestors1.iterator();
				Iterator<String> it2 = ancestors2.iterator();

				while (!completo && it1.hasNext()) {
					if (celda.getAttributes().item(0).getNodeValue().split("#")[1]
							.equalsIgnoreCase(it1.next())) {
						completo = true;
					} else
						indice1 += 1;
				}
				celda = pairs2.item(i1);
				completo = false;
				while (!completo && it2.hasNext()) {
					if (celda.getAttributes().item(0).getNodeValue().split("#")[1]
							.equalsIgnoreCase(it2.next())) {

						/*
						 * salida.write("unir(con," + indice1 + "," + indice2 +
						 * ",\"" + signos.item(i1).getFirstChild()
						 * .getTextContent() + "\",");
						 */

						if (valores.item(i1).getFirstChild().getTextContent()
								.length() < 7) {

							output.write(KineticPieces.join(indice1,
									indice2, signos.item(i1).getFirstChild()
											.getTextContent(), valores.item(i1)
											.getFirstChild().getTextContent()));

							/*
							 * salida.write(valores.item(i1).getFirstChild()
							 * .getTextContent() + ");\n");
							 */
						} else {

							output.write(KineticPieces.join(indice1,
									indice2, signos.item(i1).getFirstChild()
											.getTextContent(), valores.item(i1)
											.getFirstChild().getTextContent()
											.substring(0, 6)));

							/*
							 * salida.write(valores.item(i1).getFirstChild()
							 * .getTextContent().substring(0, 6) + ");\n");
							 */
						}
						completo = true;
					} else
						indice2 += 1;
				}

			}

			// finalizar script y cerrar fichero
			// salida.write(fragmentos.tree2);


			output.write(KineticPieces.tree1);
			output.write(KineticPieces.tree2);
			output.write(KineticPieces.tree3);
			output.write(KineticPieces.treetail);

			output.close();

			// Cabecera del fichero, con las dimensiones finales del canvas
			fstream = new FileWriter(context + "/"  + session + ".html");

			output = new BufferedWriter(fstream);

			/*
			 * if (ancestros1.size() > ancestros2.size()) {
			 * salida.write(fragmentos.treehead((ancestros1.size() + 1) * 30));
			 * } else { salida.write(fragmentos.treehead((ancestros2.size() + 1)
			 * * 30)); }
			 */

			NodeList ontos = matching.getElementsByTagName("Ontology");
			String onto1name = "";
			String onto2name = "";
			if (ontos.item(0) != null){
				onto1name = ontos.item(0).getAttributes()
						.item(0).getNodeValue();
				onto2name = ontos.item(1).getAttributes()
						.item(0).getNodeValue();
			} else {
				onto1name = pairs1.item(0).getAttributes().item(0).getNodeValue().split("#")[0];
				onto2name = pairs2.item(0).getAttributes().item(0).getNodeValue().split("#")[0];
					//onto1name = matching.getElementsByTagName("uri1").item(0).getTextContent();
					//onto2name = matching.getElementsByTagName("uri2").item(0).getTextContent();
			}
			
			
			if (ancestors1.size() > ancestors2.size()) {
				output.write(KineticPieces.treehead((ancestors1.size() + 1) * 30, onto1name, onto2name));
			} else {
				output.write(KineticPieces.treehead((ancestors2.size() + 1) * 30, onto1name, onto2name));
			}

			BufferedReader temp = new BufferedReader(new FileReader(context
					+ "/" + session + ".tmp"));
			String line;
			while ((line = temp.readLine()) != null) {
				output.write(line);
				output.newLine();
			}
			temp.close();
			output.close();

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	private static void tablemode(String ontology1, String ontology2,
			String alignment) {

		FileWriter fstream;
		try {
			fstream = new FileWriter(context + "/" + session + ".tmp");

			BufferedWriter salida = new BufferedWriter(fstream);

			// Cabecera del fichero final

			// salida.write("<!DOCTYPE html>\n" + "<html>\n" + "<body>\n");

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document onto1 = docBuilder.parse(new File(ontology1));
			Document onto2 = docBuilder.parse(new File(ontology2));
			
			//Document onto1 = docBuilder.parse(new File(context + "temps/" + ontology1));
			//Document onto2 = docBuilder.parse(new File(context + "temps/" + ontology2));
			
			boolean hayEtiquetas = false;
			boolean hayEtiquetas1 = true;
			boolean hayEtiquetas2 = true;
			
			//NodeList paresfila = onto1.getElementsByTagName("owl:Class");
			//NodeList parescolumna = onto2.getElementsByTagName("owl:Class");
			NodeList paresfila = onto1.getElementsByTagName("rdfs:label");
			
			if (paresfila.getLength() == 0){
				paresfila = onto1.getElementsByTagName("owl:Class");
				hayEtiquetas1 = false;
			}
			
			NodeList parescolumna = onto2.getElementsByTagName("rdfs:label");
			
			if (parescolumna.getLength() == 0){
				parescolumna = onto2.getElementsByTagName("owl:Class");
				hayEtiquetas2 = false;
			}
			
			int long1 = paresfila.getLength();
			int long2 = parescolumna.getLength();

			// salida.write(fragmentos.table1);
			//salida.write("var filas = [");
			//salida.write("filas = [");
			
			Document matching = docBuilder.parse(new File(alignment));
			//Document matching = docBuilder.parse(new File(context + "temps/" + alignment));

			// LinkedHashSet<String> ancestros2 = new LinkedHashSet<String>();
			NodeList clases;
			NodeList propiedades;
			NodeList datatypes;
			NodeList valores = matching.getElementsByTagName("measure");
			int[] filasvalores = new int[valores.getLength()];
			int[] colsvalores = new int[valores.getLength()];
			int corresponding = 0;
			LinkedHashMap<String, Integer> valoresenorden = new LinkedHashMap<String, Integer>();

			/*
			 * Determinamos qué ontología tiene más elementos
			 * 
			 * La ontología con más elementos se pondrá en vertical
			 */
			if (long1 >= long2) {
				// Las filas serán las clases de onto1
				clases = paresfila;
				propiedades = onto1.getElementsByTagName("owl:ObjectProperty");
				datatypes = onto1.getElementsByTagName("owl:DatatypeProperty");
				hayEtiquetas = hayEtiquetas1;
				paresfila = matching.getElementsByTagName("entity1");
				parescolumna = matching.getElementsByTagName("entity2");
			} else {
				// Las filas serán las clases de onto2
				clases = parescolumna;
				propiedades = onto2.getElementsByTagName("owl:ObjectProperty");
				datatypes = onto2.getElementsByTagName("owl:DatatypeProperty");
				hayEtiquetas = hayEtiquetas2;
				paresfila = matching.getElementsByTagName("entity2");
				parescolumna = matching.getElementsByTagName("entity1");
			}

			String atributo = "";
			LinkedHashSet<String> classIDs = new LinkedHashSet<String>();
			LinkedHashMap<String, String> allLabels = new LinkedHashMap<String, String>();
			LinkedHashSet<String> propIDs = new LinkedHashSet<String>();
			//LinkedHashMap<String, String> propLabels = new LinkedHashMap<String, String>();
			LinkedHashSet<String> dataIDs = new LinkedHashSet<String>();
			//LinkedHashMap<String, String> dataLabels = new LinkedHashMap<String, String>();
			int f = 0;
			int c = 0;

			if (hayEtiquetas) {
				
				for (int s = 0; s < clases.getLength(); s++) {
					Node clase = clases.item(s);
					
					//Sólo clases y propiedades de clase
					if ((clase.getParentNode().getNodeName()
							.equalsIgnoreCase("owl:Class"))
							|| (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:DatatypeProperty"))
							|| (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:ObjectProperty"))) {
					
					if (clase.getParentNode().hasAttributes()){
					
					NamedNodeMap atributos = clase.getParentNode().getAttributes();
					if (atributos.item(0).getNodeName()
							.equalsIgnoreCase("rdf:about") || atributos.item(0).getNodeName()
							.equalsIgnoreCase("rdf:ID"))
						atributo = atributos.item(0).getNodeValue();
					    if (atributo.contains("#"))
							atributo = atributo.split("#")[1];
					}
					
					if (!atributo.isEmpty()) {
						for (int k = 0; k < paresfila.getLength(); k++) {
							if (paresfila.item(k).getAttributes().item(0)
									.getNodeValue().split("#")[1]
									.equalsIgnoreCase(atributo)) {
								//filasvalores[k] = f;
								corresponding = k;
							}
						}
						
						if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:Class")){
							//classIDs = classIDs + "\"" + atributo + "\",";
							//classLabels = classLabels + "\"" + clases.item(s).getTextContent() + "\",";
							classIDs.add(atributo);
							allLabels.put(atributo, clases.item(s).getTextContent());
							valoresenorden.put(atributo, corresponding);
						} else if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:ObjectProperty")){
							//propIDs = propIDs + "\"" + atributo + "\",";
							//propLabels = propLabels + "\"" + clases.item(s).getTextContent() + "\",";
							propIDs.add(atributo);
							allLabels.put(atributo, clases.item(s).getTextContent());
							valoresenorden.put(atributo, corresponding);
						} else if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:DatatypeProperty")){
							//dataIDs = dataIDs + "\"" + atributo + "\",";
							//dataLabels = dataLabels + "\"" + clases.item(s).getTextContent() + "\",";
							dataIDs.add(atributo);
							allLabels.put(atributo, clases.item(s).getTextContent());
							valoresenorden.put(atributo, corresponding);
						} 
						
						
					}
					
					

					}
				}
				
				Iterator<String> it = classIDs.iterator();
				
				while (it.hasNext()){
					filasvalores[valoresenorden.get(it.next())] = f;
					f++;
				}
				
				it = propIDs.iterator();
				
				while (it.hasNext()){
					filasvalores[valoresenorden.get(it.next())] = f;
					f++;
				}
				
                it = dataIDs.iterator();
				
				while (it.hasNext()){
					filasvalores[valoresenorden.get(it.next())] = f;
					f++;
				}
				
			}
			else if (!hayEtiquetas) {
				// Escribir las filas con la ontologia mas grande
				for (int s = 0; s < (clases.getLength()+propiedades.getLength()+datatypes.getLength()); s++) {
					
					Node clase;
					
					if (s < clases.getLength()) {
						clase = clases.item(s);
					} else if(clases.getLength() <= s && s < (clases.getLength()+propiedades.getLength())){
						clase = propiedades.item(s - (clases.getLength()));
					} else {
						clase = datatypes.item(s - (clases.getLength()+propiedades.getLength()));
					}
						if (clase.hasAttributes()) {
							NamedNodeMap atributos = clase.getAttributes();
							if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:ID")) {
								atributo = atributos.item(0).getNodeValue();

								for (int k = 0; k < paresfila.getLength(); k++) {
									if (paresfila.item(k).getAttributes()
											.item(0).getNodeValue().split("#")[1]
											.equalsIgnoreCase(atributo)) {
										filasvalores[k] = f;
									}
								}
								f++;
								// salida.write("fila(ctx,\"" + atributo +
								// "\");\n");

								// salida.write("\"" + atributo + "\",");
								// salida.write("[\"" + atributo + "\", $],");
								if (s < clases.getLength()) {
									//classIDs = classIDs + "\"" + atributo + "\",";
									//classLabels = classLabels + "\"$\",";
									classIDs.add(atributo);
									allLabels.put(atributo, "$");
								} else if(clases.getLength() <= s && s < (clases.getLength()+propiedades.getLength())){
									//propIDs = propIDs + "\"" + atributo + "\",";
									//propLabels = propLabels + "\"$\",";
									propIDs.add(atributo);
									allLabels.put(atributo, "$");
								} else {
									//dataIDs = dataIDs + "\"" + atributo + "\",";
									//dataLabels = dataLabels + "\"$\",";
									dataIDs.add(atributo);
									allLabels.put(atributo, "$");
								}
								
							} else if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:about")) {
								Node padre = clase.getParentNode();
								if (padre != null
										&& (padre.getNodeName().equalsIgnoreCase("owl:Ontology")
										 || padre.getNodeName().equalsIgnoreCase("rdfs:subClassOf")
								         || padre.getNodeName().equalsIgnoreCase("rdf:RDF"))) {
									atributo = atributos.item(0).getNodeValue();
									if (atributo.contains("#")) {
										atributo = atributo.split("#")[1];
										for (int k = 0; k < paresfila
												.getLength(); k++) {
											if (paresfila.item(k)
													.getAttributes().item(0)
													.getNodeValue().split("#")[1]
													.equalsIgnoreCase(atributo)) {
												filasvalores[k] = f;
											}
										}
										f++;
										// salida.write("fila(ctx,\"" + atributo
										// +
										// "\");\n");

										// salida.write("\"" + atributo +
										// "\",");
										// salida.write("[\"" + atributo +
										// "\", $],");
										if (s < clases.getLength()) {
											//classIDs = classIDs + "\"" + atributo + "\",";
											//classLabels = classLabels + "\"$\",";
											classIDs.add(atributo);
											allLabels.put(atributo, "$");
										} else if(clases.getLength() <= s && s < (clases.getLength()+propiedades.getLength())){
											//propIDs = propIDs + "\"" + atributo + "\",";
											//propLabels = propLabels + "\"$\",";
											propIDs.add(atributo);
											allLabels.put(atributo, "$");
										} else {
											//dataIDs = dataIDs + "\"" + atributo + "\",";
											//dataLabels = dataLabels + "\"$\",";
											dataIDs.add(atributo);
											allLabels.put(atributo, "$");
										}
									}
								}
							}
						}
					
				}
				
			}
			// salida.write(fragmentos.table2);
			// salida.write(fragmentosKinetic.table1);
			salida.write("var rowsClassIDs = "+
			             classIDs.toString().replace(", ", "\",\"").replace("[", "[\"").replace("]", "\"]")
			             +";\n");
			salida.write("var rowsClassLabels = [");
			Iterator<String> it = classIDs.iterator();
			while(it.hasNext()){
				salida.write("\""+allLabels.get(it.next())+"\",");
			}
			salida.write("0];\n");
			
			salida.write("var rowsPropIDs = "+
		             propIDs.toString().replace(", ", "\",\"").replace("[", "[\"").replace("]", "\"]")
		             +";\n");
			salida.write("var rowsPropLabels = [");
			it = propIDs.iterator();
			while (it.hasNext()) {
				salida.write("\"" + allLabels.get(it.next()) + "\",");
			}
			salida.write("0];\n");
			
			salida.write("var rowsDataIDs = "+
		             dataIDs.toString().replace(", ", "\",\"").replace("[", "[\"").replace("]", "\"]")
		             +";\n");
			salida.write("var rowsDataLabels = [");
			it = dataIDs.iterator();
			while (it.hasNext()) {
				salida.write("\"" + allLabels.get(it.next()) + "\",");
			}
			salida.write("0];\n");
			
			//salida.write("0];\n" + "columnas = [");

			salida.write("function inicializar(){\n");
			
			salida.write("colsValues = [");
			//salida.write("sessionStorage.colsValues = [");
			//salida.write("columns = [");
			
			classIDs.clear();
			propIDs.clear();
			dataIDs.clear();
			allLabels.clear();
			valoresenorden.clear();
			
			//classIDs = "";
			//classLabels = "";
			// Apuntar los números de fila de cada pareja
			//f = 0;
			
			//TODO coger propiedades y datos
			if (long1 >= long2) {
				// Las columnas serán las clases de onto2
				if (hayEtiquetas2) {
					clases = onto2.getElementsByTagName("rdfs:label");
				} else
					clases = onto2.getElementsByTagName("owl:Class");
				hayEtiquetas = hayEtiquetas2;
				propiedades = onto2.getElementsByTagName("owl:ObjectProperty");
				datatypes = onto2.getElementsByTagName("owl:DatatypeProperty");
			} else {
				// Las columnas serán las clases de onto2
				if (hayEtiquetas1) {
					clases = onto1.getElementsByTagName("rdfs:label");
				} else
					clases = onto1.getElementsByTagName("owl:Class");
				hayEtiquetas = hayEtiquetas1;
				propiedades = onto1.getElementsByTagName("owl:ObjectProperty");
				datatypes = onto1.getElementsByTagName("owl:DatatypeProperty");
			}

			NodeList signos = matching.getElementsByTagName("relation");

			
				// Escribir las columnas con la otra ontologia
			if (hayEtiquetas) {
				for (int s = 0; s < clases.getLength(); s++) {
					Node clase = clases.item(s);
					if ((clase.getParentNode().getNodeName()
							.equalsIgnoreCase("owl:Class"))
							|| (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:DatatypeProperty"))
							|| (clase.getParentNode().getNodeName()
									.equalsIgnoreCase("owl:ObjectProperty"))) {
						if (clase.getParentNode().hasAttributes()) {
							NamedNodeMap atributos = clase.getParentNode()
									.getAttributes();
							if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:about")
									|| atributos.item(0).getNodeName()
											.equalsIgnoreCase("rdf:ID"))
								atributo = atributos.item(0).getNodeValue();
							if (atributo.contains("#"))
								atributo = atributo.split("#")[1];
						}
						
						for (int k = 0; k < parescolumna.getLength(); k++) {
							if (parescolumna.item(k).getAttributes().item(0)
									.getNodeValue().split("#")[1]
									.equalsIgnoreCase(atributo)) {
								corresponding = k;
							}
						}
						
						if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:Class")) {
							// classIDs = classIDs + "\"" + atributo + "\",";
							// classLabels = classLabels + "\"" +
							// clases.item(s).getTextContent() + "\",";
							classIDs.add(atributo);
							allLabels.put(atributo, clases.item(s)
									.getTextContent());
							valoresenorden.put(atributo, corresponding);
						} else if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:ObjectProperty")) {
							// propIDs = propIDs + "\"" + atributo + "\",";
							// propLabels = propLabels + "\"" +
							// clases.item(s).getTextContent() + "\",";
							propIDs.add(atributo);
							allLabels.put(atributo, clases.item(s)
									.getTextContent());
							valoresenorden.put(atributo, corresponding);
						} else if (clase.getParentNode().getNodeName()
								.equalsIgnoreCase("owl:DatatypeProperty")) {
							// dataIDs = dataIDs + "\"" + atributo + "\",";
							// dataLabels = dataLabels + "\"" +
							// clases.item(s).getTextContent() + "\",";
							dataIDs.add(atributo);
							allLabels.put(atributo, clases.item(s)
									.getTextContent());
							valoresenorden.put(atributo, corresponding);
						}

						

					}
				}

				it = classIDs.iterator();
				
				while (it.hasNext()){
					colsvalores[valoresenorden.get(it.next())] = c;
					c++;
				}
				
				it = propIDs.iterator();
				
				while (it.hasNext()){
					colsvalores[valoresenorden.get(it.next())] = c;
					c++;
				}
				
                it = dataIDs.iterator();
				
				while (it.hasNext()){
					colsvalores[valoresenorden.get(it.next())] = c;
					c++;
				}
				
			} else if (!hayEtiquetas) {

				for (int s = 0; s < (clases.getLength()
						+ propiedades.getLength() + datatypes.getLength()); s++) {

					Node clase;

					if (s < clases.getLength()) {
						clase = clases.item(s);
					} else if (clases.getLength() <= s
							&& s < (clases.getLength() + propiedades
									.getLength())) {
						clase = propiedades.item(s - (clases.getLength()));
					} else {
						clase = datatypes
								.item(s
										- (clases.getLength() + propiedades
												.getLength()));
					}

					if ((clase.getNodeName().equalsIgnoreCase("owl:Class"))
							|| (clase.getNodeName()
									.equalsIgnoreCase("owl:DatatypeProperty"))
							|| (clase.getNodeName()
									.equalsIgnoreCase("owl:ObjectProperty"))) {
						if (clase.hasAttributes()) {
							NamedNodeMap atributos = clase.getAttributes();
							if (atributos.item(0).getNodeName()
									.equalsIgnoreCase("rdf:about")
									|| atributos.item(0).getNodeName()
											.equalsIgnoreCase("rdf:ID")) {
								atributo = atributos.item(0).getNodeValue();
								if (atributo.contains("#"))
									atributo = atributo.split("#")[1];
							}
						}
						// salida.write("[\"" + atributo + "\",[");
						// classIDs = classIDs + "\"" + atributo + "\",";
						// classLabels = classLabels + "\"$\",";
						if (s < clases.getLength()) {
							// classIDs = classIDs + "\"" + atributo + "\",";
							// classLabels = classLabels + "\"$\",";
							classIDs.add(atributo);
							allLabels.put(atributo, "$");
						} else if (clases.getLength() <= s
								&& s < (clases.getLength() + propiedades
										.getLength())) {
							// propIDs = propIDs + "\"" + atributo + "\",";
							// propLabels = propLabels + "\"$\",";
							propIDs.add(atributo);
							allLabels.put(atributo, "$");
						} else {
							// dataIDs = dataIDs + "\"" + atributo + "\",";
							// dataLabels = dataLabels + "\"$\",";
							dataIDs.add(atributo);
							allLabels.put(atributo, "$");
						}
						// salida.write("[\"$\" ,[");
						// f=f+ atributo.length();
						for (int k = 0; k < parescolumna.getLength(); k++) {
							if (parescolumna.item(k).getAttributes().item(0)
									.getNodeValue().split("#")[1]
									.equalsIgnoreCase(atributo)) {
								colsvalores[k] = c;
							}
						}
						c++;
					}
				}
			}
				
				for (int k = 0; k < parescolumna.getLength(); k++) {

						salida.write("["
								// c nos cuenta las columnas
								+ colsvalores[k]
								+ ","
								+ filasvalores[k]
								+ ",\""
								+ signos.item(k).getFirstChild()
										.getTextContent() + "\",");

						if (valores.item(k).getFirstChild().getTextContent()
								.length() < 7) {
							salida.write(valores.item(k).getFirstChild()
									.getTextContent()
									+ "],");
						} else {
							salida.write(valores.item(k).getFirstChild()
									.getTextContent().substring(0, 6)
									+ "],");
						}

					
				}
			
			salida.write("0];\n"
			       +"sessionStorage.occupied = new Array();\n" +
					"};\n");
			salida.write("var colsClassIDs = "
					+ classIDs.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]") + ";\n");
			salida.write("var colsClassLabels = [");
			it = classIDs.iterator();
			while (it.hasNext()) {
				salida.write("\"" + allLabels.get(it.next()) + "\",");
			}
			salida.write("0];\n");

			salida.write("var colsPropIDs = "
					+ propIDs.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]") + ";\n");
			salida.write("var colsPropLabels = [");
			it = propIDs.iterator();
			while (it.hasNext()) {
				salida.write("\"" + allLabels.get(it.next()) + "\",");
			}
			salida.write("0];\n");

			salida.write("var colsDataIDs = "
					+ dataIDs.toString().replace(", ", "\",\"")
							.replace("[", "[\"").replace("]", "\"]") + ";\n");
			salida.write("var colsDataLabels = [");
			it = dataIDs.iterator();
			while (it.hasNext()) {
				salida.write("\"" + allLabels.get(it.next()) + "\",");
			}
			salida.write("0];\n");
			
			salida.write(KineticPieces.table1);
			salida.write(KineticPieces.table2);
			// finalizar script y cerrar fichero
			// salida.write(fragmentos.table3);
			salida.write(KineticPieces.table3);
			salida.close();

			// Cabecera del fichero, con las dimensiones finales del canvas
			fstream = new FileWriter(context + "/" + session + ".html");

			salida = new BufferedWriter(fstream);
			NodeList ontos = matching.getElementsByTagName("Ontology");
			String onto1name = "";
			String onto2name = "";
			if (ontos.item(0) != null){
			
			if (long1 >= long2) {
				onto1name = ontos.item(0).getAttributes()
						.item(0).getNodeValue();
				onto2name = ontos.item(1).getAttributes()
						.item(0).getNodeValue();
			} else {
				onto1name = ontos.item(1).getAttributes()
						.item(0).getNodeValue();
				onto2name = ontos.item(0).getAttributes()
						.item(0).getNodeValue();
			}
			} else {
				onto1name = paresfila.item(0).getAttributes().item(0).getNodeValue().split("#")[0];
				onto2name = parescolumna.item(0).getAttributes().item(0).getNodeValue().split("#")[0];
				/*
				if (long1 >= long2) {
					//onto1name = matching.getElementsByTagName("uri1").item(0).getTextContent();
					//onto2name = matching.getElementsByTagName("uri2").item(0).getTextContent();
				} else {
					//onto1name = matching.getElementsByTagName("uri2").item(0).getTextContent();
					//onto2name = matching.getElementsByTagName("uri1").item(0).getTextContent();
				}
				*/
			}
			
			salida.write(KineticPieces.tablehead(onto1name, onto2name));
			//salida.write(KineticPieces.tablehead((70 + ((c + 1) * 120)),
			//		(50 + ((f + 1) * 30)), onto1name, onto2name));

			/*
			 * salida.write(fragmentos.tablehead((70 + ((c + 1) * 120)), (50 +
			 * ((f + 1) * 30))));
			 */

			BufferedReader temp = new BufferedReader(new FileReader(context
					+ "/" + session + ".tmp"));
			String line;
			while ((line = temp.readLine()) != null) {
				salida.write(line);
				salida.newLine();
			}
			temp.close();
			salida.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}

}
