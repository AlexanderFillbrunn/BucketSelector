function KMedianSelector() {
}

KMedianSelector.prototype.select = function(models, h, k) {

  shuffle(models);

  var centers = unique(models).slice(0, Math.min(models.length, k));

  var done = false;
  var centerData;
  while(!done) {
    centerData = centers.map(c => []);
    models.forEach(m => {
      var minDist = Number.POSITIVE_INFINITY;
      var minIdx = -1;
      centers.forEach((c, i) => {
        var dist = distance(c, m);
        if (dist < minDist) {
          minIdx = i;
          minDist = dist;
        }
      });
      centerData[minIdx].push(m);
    });

    var newCenters = centerData.map(pts => selectMedoid(pts));
    var diff = centers.map((c, i) => distance(c, newCenters[i])).reduce((m, d) => m + d, 0);
    console.log(diff);
    done = diff < 0.01;
    centers = newCenters;
  }

  return centerData.map(pts => {
    var minScore = Number.POSITIVE_INFINITY;
    var minModel = null;
    pts.forEach(m => {
      var score = h(m);
      if (score < minScore) {
        minModel = m;
        minScore = score;
      }
    });
    return minModel;
  }).filter(m => m);
}

function unique(models) {
  return d3.values(models.reduce(function(memo, m) {
    var key = m.x + "_" + m.y;
    if (!memo[key]) memo[key] = m;
    return memo;
  }, {}));
}

// Voronoi method
function selectMedoid(pts) {
  var minDistSum = Number.POSITIVE_INFINITY;
  var newMedoidIdx = -1;
  pts.forEach((p1, i) => {
    var distSum = pts.reduce((m, p) => m + distance(p1, p), 0);
    if (distSum < minDistSum) {
      minDistSum = distSum;
      newMedoidIdx = i;
    }
  });
  return pts[newMedoidIdx];
}

function distance(a, b) {
  return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
}

function shuffle(array) {
    let counter = array.length;

    // While there are elements in the array
    while (counter > 0) {
        // Pick a random index
        let index = Math.floor(Math.random() * counter);

        // Decrease counter by 1
        counter--;

        // And swap the last element with it
        let temp = array[counter];
        array[counter] = array[index];
        array[index] = temp;
    }

    return array;
}
