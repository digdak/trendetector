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
}

function highlightConnected(s) {		
  	// We first need to save the original colors of our
	// nodes and edges, like this:
	s.graph.nodes().forEach(function(n) {
		n.originalColor = n.color;
	});
	s.graph.edges().forEach(function(e) {
		e.originalColor = e.color;
	});

	// When a node is clicked, we check for each node
	// if it is a neighbor of the clicked one. If not,
	// we set its color as grey, and else, it takes its
	// original color.
	// We do the same for the edges, and we only keep
	// edges that have both extremities colored.
	s.bind('clickNode', function(e) {      	
		var nodeId = e.data.node.id,
		    toKeep = s.graph.neighbors(nodeId);
		toKeep[nodeId] = e.data.node;

		get_article_with_keyword(nodeId);

		s.graph.nodes().forEach(function(n) {
			if (toKeep[n.id])
				n.color = "#44B8FF";
			else
				n.color = '#eee';
		});

		// set color node clicked
		e.data.node.color = '#ec5148';

		s.graph.edges().forEach(function(e) {
			if (toKeep[e.source] && toKeep[e.target])
				e.color = "#ec5148";
			else
				e.color = '#eee';
		});

		s.stopForceAtlas2();
		// Since the data has been modified, we need to
		// call the refresh method to make the colors
		// update effective.
		s.refresh();
	});

	// When the stage is clicked, we just color each
	// node and edge with its original color.
	s.bind('clickStage', function(e) {      	
		s.graph.nodes().forEach(function(n) {
		  n.color = n.originalColor;
		});

		s.graph.edges().forEach(function(e) {
		  e.color = e.originalColor;
		});

		// Same as in the previous event:
		s.refresh();
	});
}

function force_power(sigInst) {
	config = {};
	config.barnesHutOptimize = false;
	config.strongGravityMode = true;
	config.edgeWeightInfluence = 1;	
	sigInst.startForceAtlas2(config);
}	

sigma.classes.graph.addMethod('neighbors', function(nodeId) {		
    var k,
        neighbors = {},
        index = this.allNeighborsIndex[nodeId] || {};

    for (k in index)
      neighbors[k] = this.nodesIndex[k];

    return neighbors;
});


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
	    x: Math.random(),
	    y: Math.random(),
	    size: 1,
	    color: '#666'
	  });	

	var keywords = nodes.map(function(item) {
		return item._node._data.data.id;
	});
	// console.log(keywords);
	for (i = 0; i < E; i++)
	  // if (keywords.indexOf(relations[i][0]) == -1 || keywords.indexOf(relations[i][1]) == -1) {
	  // 	console.log("duplicated " + relations[i][0] +  "   " + relations[i][1]);
	  // 	continue;
	  // }

	  g.edges.push({
	    id: 'e' + i,
	    source: relations[i][0],
	    target: relations[i][1],
	    size: 1,
	    color: '#ccc'
	  });	
	// Instantiate sigma:

	s = new sigma({
	  graph: g,
	  container: 'graph-container'
	});
	highlightConnected(s);
	force_power(s);
	// console.log("create graph called");
}

function getGraph(term, batch_time) {
	// console.log("getGraph term =" + term + "  batch_time = " + batch_time);
	$.getJSON('/graph/nodes_with_rel/'+term+'/'+batch_time, function (data) {
		// console.log(data);
		nodes = data.keywords;
		relations = data.relations;
		$("#graph-container").html("");
		createGraph(nodes, relations);
	});
}
