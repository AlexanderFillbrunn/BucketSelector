function BucketSelector() {

}

BucketSelector.prototype.select = function(models, h, k) {

  var buckets = [];

  models.forEach(function(m, i) {
    var hash = md5.create();
    hash.update(m.x.toString());
    hash.update(m.y.toString());
    var b = hash.hex().hashCode() % k;

    if (!buckets[b] || h(buckets[b]) > h(m)) {
      buckets[b] = m;
    }
  });
  var res = buckets.filter(b => b);
  return res;
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
