(function() {

  var SELECTORS = {
    "Simple Hash Bucket" : new BucketSelector(),
    "Random Bucket" : new RandomBucketSelector(),
    "Hash Multibucket" : new BucketSelector2(2),
    "Random Uniform Size Bucket" : new BucketSelector3(),
    "Top-k" : new TopKSelector(),
    "Top-k Diverse" : new DiverseTopKSelector(),
    "Score Erosion" : new ScoreErosionSelector(),
    "Random" : new RandomSelector(),
    "K-Medoid" : new KMedianSelector(),
    "Locality Sensitive Bucket Selector" : new LocalitySensitiveBucketSelector(),
    //"Diverse Top-k + Random Uniform Size Bucket" : new CombinedSequentialSelector([new DiverseTopKSelector(), new BucketSelector3()], [5,5]),
  };

  var colors = function(i) {
    return COLOR_VALUES[i];
  }

  var start = {x : 50, y : 0};

  var deduplicate = true;
  var winners = [];
  var refinementRadius = 1;
  var allowDiagonal = true;
  var width = 100;
  var height = 100;
  var _tiles;

  var solution = null;
  var done = false;
  var K = 30;
  var mousedown = false;
  var SQRT_2 = Math.sqrt(2);
  var SQRT_5 = Math.sqrt(5);
  var dragMode = true;
  var maxActivation = 0;

  var targets = [
    {x : 80, y : 80, size : 10, strength : 3.5},
    {x : 45, y : 60, size : 12, strength : 2},
    {x : 60, y : 30, size : 10, strength : 1.5},
    {x : 20, y : 50, size : 10, strength : 2},
    // Solutions
    {x : 10, y : height - 1, size : 15, strength : 1},
    {x : 30, y : height - 1, size : 7, strength : 3},
    {x : 60, y : height - 1, size : 10, strength : 1},
    {x : 80, y : height - 1, size : 5, strength : 0.5}
  ];

  var STATE = {
    NONE : "none",
    START : "start",
    END : "end",
    WALL : "wall",
    VISITED : "visited",
    CURRENT : "current",
    SOLUTION : "solution"
  };

  d3.select("#step").on("click", function() {
    step();
  });

  d3.select("#play").on("click", function() {
    play();
  });

  d3.select("#reset").on("click", function() {
    clear();
  });

  d3.select("#dedup").on("change", function() {
    deduplicate = d3.select(this).property("checked");
  });

  function createPath(m, i) {
    var path = [];
    var p = m;
    while(p) {
      path.push([p.x, p.y]);
      p = p.parent;
    }
    path.color = "#000"
    return path;
  }

  function play() {
    step();
    if (!done) {
      requestAnimationFrame(play);
      //setTimeout(play, 100);
    } else {
      winners = [];
      models.sort((a, b) => heuristic(b) - heuristic(a)).forEach((m, i) => {
        var path = createPath(m, i);
        winners.push(path);
      });
    }
    render();
  }

  function findTile(x, y) {
    return _tiles[x][y];
  }

  function step() {
    if (done) return;
    var refined = models.reduce(function(memo, cur) {
      memo.push({x : cur.x, y : cur.y + 1, parent : cur, length : cur.length + 1});

      memo.push({x : cur.x - 1, y : cur.y + 1, parent : cur, length : cur.length + SQRT_2});
      memo.push({x : cur.x + 1, y : cur.y + 1, parent : cur, length : cur.length + SQRT_2});

      //memo.push({x : cur.x + 2, y : cur.y + 1, parent : cur, length : cur.length + SQRT_5});
      //memo.push({x : cur.x - 2, y : cur.y + 1, parent : cur, length : cur.length + SQRT_5});
      return memo;
    }, [])
    .filter(m => m.x >= 0 && m.y >= 0 && m.x < width && m.y < height
            && (_tiles[m.x][m.y].type == STATE.NONE || _tiles[m.x][m.y].type == STATE.END));

    var selector = SELECTORS[d3.select("input[name=selector]:checked").attr("value")];

    if (deduplicate) {
      refined = d3.values(refined.reduce(function(memo, m) {
        var key = m.x + "_" + m.y;
        if (!memo[key]) memo[key] = m;
        return memo;
      }, {}));
    }

    models = selector.select(refined, heuristic, K);

    solution = models.find(m => m.y == height - 1);
    done = !!solution;

    d3.selectAll(".current").each(function(t) {
      _tiles[t.x][t.y].type = STATE.VISITED;
    });

    models.forEach(m => _tiles[m.x][m.y].type = STATE.CURRENT);

    winners = [];
    models.sort((a, b) => heuristic(b) - heuristic(a)).forEach((m, i) => {
      var path = createPath(m, i);
      winners.push(path);
    });

    render();
  }

  function heuristic(m) {
    return 1 / activation(m.x, m.y);
  }

  function clear() {
    winners = [];
    done = false;
    solution = null;
    maxActivation = 0;
    for (var x = 0; x < width; x++) {
      for (var y = 0; y < height; y++) {
        if (_tiles[x][y].type !== STATE.WALL) {
            _tiles[x][y].type = STATE.NONE;
            _tiles[x][y].activation = activation(x,y);
            maxActivation = Math.max(_tiles[x][y].activation, maxActivation);
        }
      }
    }

    models = [{x : start.x, y : start.y, length : 0}];
    render();
  }

  function activation(x,y) {
    var sum = 0;
    targets.forEach(function(p) {
      sum += p.strength * Math.exp(-(Math.pow(x-p.x, 2) + Math.pow(y-p.y, 2)) / (2 * Math.pow(p.size, 2)));
    });
    return sum / 5;
  }

  function init() {

    d3.select("#k").on("input", function() {
      K = +this.value;
      d3.select("#kval").text(K);
    });

    var items = d3.select("#selectors").selectAll(".selector")
      .data(Object.keys(SELECTORS))
      .enter()
      .append("li");

      items.append("input")
      .attr("type", "radio")
      //.attr("id", d => d)
      .attr("value", d => d)
      .attr("name", "selector")
      .property("checked", (d, i) => i == 0);

      items.append("label").text(d => " " + d);

    // <input type="radio" id="bucket" name="selector" value="bucket" checked><label for="bucket"> Bucket</label><br/>
    _tiles = [];
    for (var x = 0; x < width; x++) {
      var column = [];
      for (var y = 0; y < height; y++) {
        column.push({
          type : STATE.NONE
        });
      }
      _tiles.push(column);
    }

    var layer = d3.select("#extras");

    layer.append("path")
    .attr("class", "boundary")
    .attr("fill", "#333")
    .attr("d", "M0,0 L300,0 L0,300Z");

    layer.append("path")
    .attr("class", "boundary")
    .attr("fill", "#333")
    .attr("d", "M306,0 L600,0 L600,300Z");

    clear();
  }

  function render() {
    var svg = d3.select("#map");
    var tileLayer = svg.select("#tiles");
    var extraLayer = svg.select("#extras");

    var scaleX = d3.scaleBand().domain(_tiles.map((t,i) => i)).range([0, 600]);
    var scaleY = d3.scaleBand().domain(_tiles[0].map((t,i) => i)).range([0, 600]);

    var tileData = _tiles.reduce((memo, column, c) => {
      return memo.concat(column.map((t, r) =>
        {return { x : c, y : r, type : t.type, activation : t.activation, color : t.color }}));
    }, [])

    var tiles = tileLayer.selectAll("rect.tile").data(tileData, t => t.x + "_" + t.y);

    tiles.enter().append("rect")
    .attr("x", t => scaleX(t.x))
    .attr("y", t => scaleX(t.y))
    .attr("width", scaleX.bandwidth())
    .attr("height", scaleY.bandwidth())
    .merge(tiles)
    .attr("class", t => "tile " + t.type)
    .attr("fill", t => {
      if (t.type === STATE.NONE) {
        var act = t.activation / maxActivation;
        var c = 255 - Math.floor(act * 255);
        return d3.rgb(c, c, c);
      } else if (t.type === STATE.VISITED) {
        var act = t.activation / maxActivation;
        var c = 255 - Math.floor(act * 255);
        return d3.rgb(50, c, 50);
      } else if (t.type === STATE.SOLUTION) {
        return t.color;
      }
      return null;
    });

    var line = d3.line().x(d => d[0] * scaleX.bandwidth() + scaleX.bandwidth() / 2)
                        .y(d => d[1] * scaleY.bandwidth() + scaleY.bandwidth() / 2)
                        .curve(d3.curveCardinal);
  }
  init();
})();
