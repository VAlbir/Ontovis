<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Guardar copia de alineamiento</title>
</head>
<body>
<%@ page import="java.io.FileWriter"%>
<%@ page import="java.io.File"%>
<%@ page import="java.io.BufferedWriter"%>
<%
	String entrada = request.getParameter("q");
	//File tmpdir = (File)application.getAttribute("javax.servlet.context.tempdir");
	BufferedWriter salida = new BufferedWriter(new FileWriter(new File(
			this.getServletContext().getRealPath(""), "alignment.rdf"),
			false));
	//salida.write(cambios.split("@")[0]+"\n"+cambios.split("@")[1]+"\n"+cambios.split("@")[2]+"\n");
	String[] filas = entrada.split("@")[0].split(",");
	String[] columnas = entrada.split("@")[1].split(",");
	String[] valorCols = entrada.split("@")[2].split(",");
	String onto1 = entrada.split("@")[3];
	String onto2 = entrada.split("@")[4];
	// 	salida.write("FILAS:" + entrada.split("@")[0] + "\n");
	// 	salida.write("COLUMNAS:" + entrada.split("@")[1] + "\n");

	salida.write("<?xml version='1.0' encoding=\"utf-8\" standalone='no'?>\n"
			+ "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'\n"
			+ "         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n"
			+ "         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'\n"
			+ "         xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'>\n"
			+ "<Alignment>\n"
			+ "<onto1>\n"
			+ "  <Ontology rdf:about=\""
			+ onto1
			+ "\">\n"
			+ "  </Ontology>\n"
			+ "</onto1>\n"
			+ "<onto2>\n"
			+ "  <Ontology rdf:about=\""
			+ onto2
			+ "\">\n"
			+ "  </Ontology>\n" + "</onto2>\n");
	int i = 0;
	while (i+3 < valorCols.length) {
		//System.out.println(entity2+", "+columnas[i + k]+", "+columnas[i + k + 1]+", "+columnas[i + k + 2]);
		salida.write("<map>\n"
				+ " <Cell>\n"
				+ "   <entity1 rdf:resource='"
				+ onto1
				+ "#"
				+ filas[Integer.valueOf(valorCols[i + 1])]
				+ "'/>\n"
				+ "   <entity2 rdf:resource='"
				+ onto2
				+ "#"
				+ columnas[Integer.valueOf(valorCols[i])]
				+ "'/>\n"
				+ "   <relation>"
				+ valorCols[i + 2]
				+ "</relation>\n"
				+ "   <measure rdf:datatype='http://www.w3.org/2001/XMLSchema#float'>"
				+ valorCols[i + 3] + "</measure>\n" + " </Cell>\n"
				+ "</map>\n");
		i += 4;
	}
	salida.write("</Alignment>\n" + "</rdf:RDF>");
	salida.flush();
	salida.close();
	//response.sendRedirect(request.getContextPath()+"/alignment.rdf");
%>
</body>
</html>
