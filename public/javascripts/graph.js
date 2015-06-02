function createRandomGraph() {	
	var i,
    s,
    N = 100,
    E = 500,
    g = {
      nodes: [],
      edges: []
    };
	// Generate a random graph:
	for (i = 0; i < N; i++)
	  g.nodes.push({
	    id: 'n' + i,
	    label: 'Node ' + i,
	    x: Math.random(),
	    y: Math.random(),
	    size: Math.random(),
	    color: '#666'
	  });
	for (i = 0; i < E; i++)
	  g.edges.push({
	    id: 'e' + i,
	    source: 'n' + (Math.random() * N | 0),
	    target: 'n' + (Math.random() * N | 0),
	    size: Math.random(),
	    color: '#ccc'
	  });
	// Instantiate sigma:
	s = new sigma({
	  graph: g,
	  container: 'graph-container'
	});
	console.log("create called");
}

function createGraph(nodes, relations) {
	var i,
    s,
    N = nodes.length,
    E = relations.length,
    g = {
      nodes: [],
      edges: []
    };
	// Generate a random graph:
	for (i = 0; i < N; i++)
	  g.nodes.push({
	    id: nodes[i]._node._data.data.id,
	    label: nodes[i]._node._data.data.id,
	    x: Math.random()%50,
	    y: Math.random()%50,
	    size: 10,
	    color: '#666'
	  });
	console.log(g);
	for (i = 0; i < E; i++)
	  g.edges.push({
	    id: 'e' + i,
	    source: relations[i][0],
	    target: relations[i][1],
	    size: Math.random(),
	    color: '#ccc'
	  });
	console.log(g);
	// Instantiate sigma:
	s = new sigma({
	  graph: g,
	  container: 'graph-container'
	});
	console.log("create graph called");
}

function getGraph() {
	$.getJSON('/graph/nodes_with_rel/keyword_24_72', function (data) {
		console.log("in getGraph");
		console.log(data);
		nodes = data.keywords;
		relations = data.relations;
		createGraph(nodes, relations);
	});
}

getGraph();