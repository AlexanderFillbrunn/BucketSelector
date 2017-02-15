function CombinedSequentialSelector(selectors, plan) {
  this.selectors = [];
  for(var i = 0; i < plan.length; i++) {
    for(var j = 0; j < plan[i]; j++) {
      this.selectors.push(selectors[i]);
    }
  }
  this.idx = 0;
}

CombinedSequentialSelector.prototype.select = function(models, h, k) {
  var ret = this.selectors[this.idx].select(models, h, k);
  this.idx = (this.idx + 1) % this.selectors.length;
  return ret;
}
