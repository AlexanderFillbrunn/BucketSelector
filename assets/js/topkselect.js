function TopKSelector() {
}

TopKSelector.prototype.select = function(models, h, k) {
  var sorted = models.slice(0).sort((a, b) => h(a) - h(b));
  return sorted.slice(0, Math.min(sorted.length, k))
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
