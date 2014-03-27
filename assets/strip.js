var fs = require("fs");
var obj = JSON.parse(fs.readFileSync("Artist.json").toString('utf-8'));

var artistList = [];
for(var i = 0; i < obj.results.length; i++){
	var result = obj.results[i];
	var artist = {
		id:result.objectId,
		name:result.Name,
		bio:result.Bio,
		picture:result.Picture.name
	};
	artistList.push(artist);
}

fs.writeFileSync("artist_data.json",JSON.stringify({artists:artistList}));

var obj = JSON.parse(require("fs").readFileSync("Painting.json").toString('utf-8'));
var paintingList = [];
for(var i = 0; i < obj.results.length; i++){
	var result = obj.results[i];
	var painting = {
		artistID:result.ArtistID,
		description:result.PaintingDescription,
		picture:result.PaintingFile.name,
		name:result.PaintingName,
		type:result.PaintingType,
		year:result.PaintingYear,
		recognizeID:result.RecognizeID,
		id:result.objectId
	};
	paintingList.push(painting);
}

fs.writeFileSync("painting_data.json",JSON.stringify({paintings:paintingList}));
