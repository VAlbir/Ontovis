<html>
<title>Welcome to OntoVis</title>
<head>
</head>
<body>
<img src="./logoOntoVis3.png" alt="OntoVis">
<!-- <form action="selector" method="POST"> -->
<form action="selector" method="POST" enctype="multipart/form-data">
Welcome to OntoVis: Online Ontology Alignment Visualizer
<br />
<!-- Select entry mode -->
<!-- <br /> -->
<!-- <input type="radio" name="inputO1" value="url"></input> URL -->
<!-- <input type="radio" name="inputO1" value="file"></input> FILE -->
<br />
<!-- Ontology1: <input type="text" size=70 value="http://oaei.ontologymatching.org/2011/benchmarks/101/onto.rdf" name="ontology1"> -->
Ontology1: 
<br />
           <input type="radio" name="inputO1" id="foo1" value="url" required=true> URL:
           <input type="text" size=70 name="onto1url" onClick="document.getElementById('foo1').checked = true;"
           onKeyUp="document.getElementById('foo1').checked = true;"></input>
           <br />
           <input type="radio" name="inputO1" id="foo2" value="file" required=true> FILE:
           <input type="file" size=30 name="onto1file" onClick="document.getElementById('foo2').checked = true;">
           
<br />
<br />
Ontology2:
<br />
           <input type="radio" name="inputO2" id="foo3" value="url" required=true> URL: 
           <input type="text" size=70 name="onto2url" onClick="document.getElementById('foo3').checked = true;"
           onKeyUp="document.getElementById('foo3').checked = true;"></input>
           <br />
           <input type="radio" name="inputO2" id="foo4" value="file" required=true> FILE: 
           <input type="file" size=30 name="onto2file" onClick="document.getElementById('foo4').checked = true;"></input>
           
<br />
<!-- Ontology2: <input type="text" size=70 value="http://oaei.ontologymatching.org/2011/benchmarks/206/onto.rdf" name="ontology2" /> -->
<br />
Alignment: 
<br />
           <input type="radio" name="inputAl" id="foo5" value="url" required=true> URL: 
           <input type="text" size=70 name="alignurl" onClick="document.getElementById('foo5').checked = true;"
           onKeyUp="document.getElementById('foo5').checked = true;"></input>
           <br />
           <input type="radio" name="inputAl" id="foo6" value="file" required=true> FILE: 
           <input type="file" size=30 name="alignfile" onClick="document.getElementById('foo6').checked = true;"></input>
<br />
<br />
Mode: <input type="radio" name="mode" value="tree" checked=true required=true>Tree
      <input type="radio" name="mode" value="table" required=true>Table<br>
<!-- Alignment: <input type="text" size=70 value="http://oaei.ontologymatching.org/2011/benchmarks/206/refalign.rdf" name="alignment" />-->
<input type="submit" value="Submit" /></input>
</form>
How to use:<br />
- Select .rdf files corresponding to both ontologies and the alignment between them.<br />
- You may either put a link to each .rdf file (check URL button)<br />
or upload .rdf files from your device. (check FILE button)<br />
- "Tree mode" is parallel tree visualization. <a href="http://oeg-lia3.dia.fi.upm.es:8080/ontovis/exampletree.html">Example</a> <br />
- "Table mode" is adjacency table visualization.<a href="http://oeg-lia3.dia.fi.upm.es:8080/ontovis/exampletable.html">Example</a> <br />
</body>
</html>
