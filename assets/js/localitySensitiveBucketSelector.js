function LocalitySensitiveBucketSelector() {
  this.deduplicate = true;
}

var epoch = 1;
LocalitySensitiveBucketSelector.prototype.select = function(models, h, k) {

  var buckets = [];

  var minRange = Math.max(0, 50 - epoch);
  var maxRange = Math.min(100, 50 + epoch);

  var max = Math.min(maxRange - minRange + 1, 100);
  var step = max / k;
  epoch++;
  models.forEach(function(m, i) {
    var b = (step < 1.0) ? m.x : Math.floor((m.x - minRange) / step);
    if (!buckets[b] || h(buckets[b]) > h(m)) {
      buckets[b] = m;
    }
  });

  var res = buckets.filter(b => b);

  if (res.length < k) {
    let sorted = models.slice(0).sort((a, b) => h(a) - h(b)).slice(0, Math.min(res.length, k - res.length) + 1);
    res = res.concat(sorted);
  }

  return res;
}
