
function getUserLocation(){
	if (navigator.geolocation) {
		window.onload = function() {
			var startPos;
            console.log(startPos)
			var geoSuccess = function(position) {
				startPos = position;
				const latitudue = startPos.coords.latitude;
                document.getElementById("divLatitude").value = latitudue
				
                const longitude = startPos.coords.longitude;
                document.getElementById("divLongitude").value = longitude

                document.getElementById("geoHash").value = geohash.encode(latitudue, longitude)
			};
			var geoError = function(error) {
                console.log(error)
                document.getElementById("divLongitude").innerHTML = "Please enable location"
                document.getElementById("submitButton").style.display = "none"
			};
			navigator.geolocation.getCurrentPosition(geoSuccess, geoError);
		};
	} else {
        console.log("Browser does not support location")
        document.getElementById("divLongitude").innerHTML = "Browser does not support getting location"
        document.getElementById("submitButton").style.display = "none"
	}
}