function ScoreErosionSelector() {
  this.beta = 1;
}

ScoreErosionSelector.prototype.select = function(models, h, k) {
  models.forEach(m => m.erosion = 1);
  function ah(model) {
    if (model.selected) return Number.POSITIVE_INFINITY;
    return model.erosion * h(model);
  }

  var best = models.reduce((b, model) => (!b || h(model) < h(b)) ? model : b, null);
  var sel = [];
  for(let i = 0; i < k; i++) {
    sel.push(best);
    best.selected = true;
    newBest = best;
    models.forEach(m => {
      if (m.selected) return;
      m.erosion = m.erosion / (1 - Math.exp(-distance(m, best) / this.beta));
      if (ah(m) < ah(newBest)) {
        newBest = m;
      }
    });
    best = newBest;
  }

  return sel;
}

function distance(a, b) {
  return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
}
