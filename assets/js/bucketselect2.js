function BucketSelector2(f = 2) {
  this.f = f;
}

BucketSelector2.prototype.select = function(models, h, k) {

  var buckets = [];

  models.forEach(function(m, i) {
    var hash = md5.create();
    hash.update(m.x.toString());
    hash.update(m.y.toString());
    var b = hash.hex().hashCode() % (k * 2);

    if (!buckets[b] || h(buckets[b].model) > h(m)) {
      buckets[b] = {
        model : m,
        count : (buckets[b] ? (buckets[b].count + 1) : 1)
      };
    }
  });

  buckets = buckets.filter(b => b);
  while(buckets.length > k) {
    buckets.sort((a, b) => a.count - b.count);
    var b1 = buckets.shift();
    var b2 = buckets.shift();
    buckets.unshift(mergeBuckets(b1, b2, h));
  }

  return buckets.map(b => b.model);
}

function mergeBuckets(b1, b2, h) {
  return {
    count : b1.count + b2.count,
    model : (h(b1.model) < h(b2.model)) ? b1.model : b2.model
  };
}

String.prototype.hashCode = function() {
  var hash = 0, i, chr, len;
  if (this.length === 0) return hash;
  for (i = 0, len = this.length; i < len; i++) {
    chr   = this.charCodeAt(i);
    hash  = ((hash << 5) - hash) + chr;
    hash |= 0; // Convert to 32bit integer
  }
  return Math.abs(hash);
};
