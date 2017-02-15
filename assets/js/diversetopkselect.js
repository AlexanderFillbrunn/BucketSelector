function DiverseTopKSelector() {
}

DiverseTopKSelector.prototype.select = function(models, h, k) {

  if (models.length <= k) {
    return models;
  }

  var sorted = models.sort((a, b) => h(a) - h(b));

  var selected = [];

  for (var i = 0; i < sorted.length; i++) {
    var model = sorted[i];
    var ok = true;
    for (var j = 0; j < selected.length; j++) {
      if (Math.sqrt(Math.pow(model.x - selected[j].x, 2) + Math.pow(model.y - selected[j].y, 2)) < 3) {
        ok = false;
        break;
      }
    }
    if (ok) {
      selected.push(model);
    }
    if (selected.length == k) {
      return selected;
    }
  }

  if (selected.length < k) {
    for (var i = 1; i < sorted.length && selected.length <= k; i++) {
      var model = sorted[i];
      var ok = true;
      for (var j = 0; j < selected.length; j++) {
        var s = selected[j];
        if (model === s) {
          ok = false;
          break;
        }
      }
      if (ok) {
        selected.push(model);
      }
    }
  }

  return selected;
}
