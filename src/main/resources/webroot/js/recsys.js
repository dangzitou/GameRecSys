
function appendMovie2Row(rowId, movie, baseUrl) {

    var genresStr = "";
    if (movie.genres) {
        $.each(movie.genres, function(i, genre){
            genresStr += ('<div class="genre"><a href="'+baseUrl+'collection.html?type=genre&value='+genre+'"><b>'+genre+'</b></a></div>');
        });
    }

    var imageUrl = movie.headerImage || ('./posters/' + movie.movieId + '.jpg');
    var rating = movie.averageRating ? movie.averageRating.toPrecision(2) : "N/A";
    var year = movie.releaseYear || "";

    var divstr = '<div class="movie-row-item" style="margin-right:5px">\
                    <movie-card-smart>\
                     <movie-card-md1>\
                      <div class="movie-card-md1">\
                       <div class="card">\
                        <link-or-emit>\
                         <a uisref="base.movie" href="./movie.html?movieId='+movie.movieId+'">\
                         <span>\
                           <div class="poster">\
                            <img src="' + imageUrl + '" style="width:100%; height:100%; object-fit: cover;" />\
                           </div>\
                           </span>\
                           </a>\
                        </link-or-emit>\
                        <div class="overlay">\
                         <div class="above-fold">\
                          <link-or-emit>\
                           <a uisref="base.movie" href="./movie.html?movieId='+movie.movieId+'">\
                           <span><p class="title">' + movie.title + '</p></span></a>\
                          </link-or-emit>\
                          <div class="rating-indicator">\
                           <ml4-rating-or-prediction>\
                            <div class="rating-or-prediction predicted">\
                             <svg xmlns:xlink="http://www.w3.org/1999/xlink" class="star-icon" height="14px" version="1.1" viewbox="0 0 14 14" width="14px" xmlns="http://www.w3.org/2000/svg">\
                              <defs></defs>\
                              <polygon fill-rule="evenodd" points="13.7714286 5.4939887 9.22142857 4.89188383 7.27142857 0.790044361 5.32142857 4.89188383 0.771428571 5.4939887 4.11428571 8.56096041 3.25071429 13.0202996 7.27142857 10.8282616 11.2921429 13.0202996 10.4285714 8.56096041" stroke="none"></polygon>\
                             </svg>\
                             <div class="rating-value">\
                              '+movie.positiveReviews+'\
                             </div>\
                            </div>\
                           </ml4-rating-or-prediction>\
                          </div>\
                          <p class="year">'+year+'</p>\
                         </div>\
                         <div class="below-fold">\
                          <div class="genre-list">\
                           '+genresStr+'\
                          </div>\
                          <div class="ratings-display">\
                           <div class="rating-average">\
                            <span class="rating-large">'+movie.positiveReviews+'</span>\
                            <span class="rating-total">Positives</span>\
                           </div>\
                          </div>\
                         </div>\
                        </div>\
                       </div>\
                      </div>\
                     </movie-card-md1>\
                    </movie-card-smart>\
                   </div>';
    $('#'+rowId).append(divstr);
};


function addRowFrame(pageId, rowName, rowId, baseUrl) {
 var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <a class="plainlink" title="go to the full list" href="'+baseUrl+'collection.html?type=genre&value='+rowName+'">' + rowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId +'" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
     $(pageId).prepend(divstr);
};

function addRowFrameWithoutLink(pageId, rowName, rowId, baseUrl) {
 var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <a class="plainlink" title="go to the full list" href="'+baseUrl+'collection.html?type=genre&value='+rowName+'">' + rowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId +'" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
     $(pageId).prepend(divstr);
};

function addGenreRow(pageId, rowName, rowId, size, baseUrl) {
    addRowFrame(pageId, rowName, rowId, baseUrl);
    $.getJSON(baseUrl + "getrecommendation?genre="+rowName+"&size="+size+"&sortby=positiveReviews", function(result){
        $.each(result, function(i, movie){
          appendMovie2Row(rowId, movie, baseUrl);
        });
    });
};


function addMovieDetails(containerId, movieId, baseUrl) {

    $.getJSON(baseUrl + "getmovie?id=" + movieId, function (movieObject) {
        var genres = "";
        if (movieObject.genres) {
            $.each(movieObject.genres, function (i, genre) {
                genres += ('<span class="genre-tag"><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '">' + genre + '</a></span>');
            });
        }

        // Media processing
        var mediaItems = [];
        
        // Add videos first
        if (movieObject.productionVideos) {
            var vids = movieObject.productionVideos.split(",");
            $.each(vids, function(i, vid){
                if(vid.trim() !== "") {
                    mediaItems.push({type: 'video', src: vid.trim()});
                }
            });
        }
        
        // Add screenshots
        if (movieObject.screenshots) {
            var shots = movieObject.screenshots.split(",");
            $.each(shots, function(i, shot){
                if(shot.trim() !== "") {
                    mediaItems.push({type: 'image', src: shot.trim()});
                }
            });
        }

        // Main Display (Default to first item)
        var mainDisplayHtml = "";
        if (mediaItems.length > 0) {
            if (mediaItems[0].type === 'video') {
                mainDisplayHtml = '<video id="main-media-video" width="100%" height="100%" controls autoplay muted><source src="' + mediaItems[0].src + '" type="video/mp4">Your browser does not support the video tag.</video><img id="main-media-image" src="" style="width:100%; height:100%; object-fit: contain; display:none;">';
            } else {
                mainDisplayHtml = '<img id="main-media-image" src="' + mediaItems[0].src + '" style="width:100%; height:100%; object-fit: contain;"><video id="main-media-video" width="100%" height="100%" controls style="display:none;"></video>';
            }
        } else {
             mainDisplayHtml = '<img id="main-media-image" src="' + (movieObject.headerImage || './posters/' + movieObject.movieId + '.jpg') + '" style="width:100%; height:100%; object-fit: contain;">';
        }

        // Slider
        var sliderHtml = "";
        $.each(mediaItems, function(i, item){
            if(item.type === 'video') {
                sliderHtml += '<div class="slider-item" onclick="changeMainMedia(\'video\', \''+item.src+'\')"><video src="'+item.src+'" style="width:100%; height:100%; object-fit: cover;" muted></video></div>';
            } else {
                sliderHtml += '<div class="slider-item" onclick="changeMainMedia(\'image\', \''+item.src+'\')"><img src="'+item.src+'" style="width:100%; height:100%; object-fit: cover;"></div>';
            }
        });

        var priceDisplay = movieObject.price ? "$" + movieObject.price : "Free";
        if (movieObject.price === "0") priceDisplay = "Free to Play";

        var movieDetails = `
            <div class="game-details-container" style="color: #c6d4df; background-color: #1b2838; padding: 20px; max-width: 1200px; margin: 0 auto;">
                <div class="row">
                    <div class="col-md-12">
                        <h1 class="game-title" style="color: #fff; margin-bottom: 20px;">${movieObject.title}</h1>
                    </div>
                </div>
                
                <div class="row">
                    <!-- Left Column: Media -->
                    <div class="col-md-8">
                        <div class="main-media-container" style="background: #000; height: 400px; display: flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                            ${mainDisplayHtml}
                        </div>
                        <div class="media-slider" style="display: flex; overflow-x: auto; gap: 5px; padding-bottom: 10px;">
                            ${sliderHtml}
                        </div>
                    </div>
                    
                    <!-- Right Column: Info -->
                    <div class="col-md-4">
                        <div class="game-info-panel">
                            <img src="${movieObject.headerImage}" style="width: 100%; margin-bottom: 20px;">
                            
                            <div class="info-row">
                                <span class="info-label">Release Date:</span>
                                <span class="info-value">${movieObject.releaseDate || 'N/A'}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Developer:</span>
                                <span class="info-value">${movieObject.developer || 'N/A'}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Publisher:</span>
                                <span class="info-value">${movieObject.publisher || 'N/A'}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Price:</span>
                                <span class="info-value price-tag">${priceDisplay}</span>
                            </div>
                             <div class="info-row">
                                <span class="info-label">Reviews:</span>
                                <span class="info-value">${movieObject.positiveReviews} Positive</span>
                            </div>
                            
                            <div class="info-section" style="margin-top: 20px;">
                                <div class="info-label">Supported Languages:</div>
                                <div class="info-value small-text" style="max-height: 100px; overflow-y: auto;">${movieObject.supportedLanguages || 'N/A'}</div>
                            </div>
                            
                            <div class="info-section" style="margin-top: 20px;">
                                <div class="info-label">Genres:</div>
                                <div class="genre-list">
                                    ${genres}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Description Section -->
                <div class="row" style="margin-top: 30px;">
                    <div class="col-md-8">
                        <div class="game-description">
                            <h3 style="color: #fff; border-bottom: 1px solid #3a4b5c; padding-bottom: 10px;">About This Game</h3>
                            <div style="font-size: 14px; line-height: 1.6; color: #acb2b8;">
                                ${movieObject.description}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        $("#"+containerId).prepend(movieDetails);
    });
};

// Helper function for media switching
window.changeMainMedia = function(type, src) {
    var vid = document.getElementById('main-media-video');
    var img = document.getElementById('main-media-image');
    
    if (type === 'video') {
        img.style.display = 'none';
        vid.style.display = 'block';
        vid.src = src;
        vid.play();
    } else {
        vid.pause();
        vid.style.display = 'none';
        img.style.display = 'block';
        img.src = src;
    }
};
