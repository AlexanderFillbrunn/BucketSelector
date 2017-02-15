function CombinedSelector(selectors) {
  this.selectors = selectors;
}

CombinedSelector.prototype.select = function(models, h, k) {
  var ret = this.selectors.reduce((m, s) => m.concat(s.select(models, h, k / this.selectors.length)), []);
  return ret;
}
