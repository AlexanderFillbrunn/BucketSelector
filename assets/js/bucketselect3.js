function BucketSelector3() {

}

BucketSelector3.prototype.select = function(models, h, k) {

  var buckets = [];

  shuffle(models).forEach(function(m, i) {
    var b = i % k;
    if (!buckets[b] || h(buckets[b]) > h(m)) {
      if(!buckets.some(bu => bu && bu.x == m.x && bu.y == m.y)) {
        buckets[b] = m;
      }
    }
  });
  var res = buckets.filter(b => b);

  //console.log(models.length + " -> " + res.length);
  return res;
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
