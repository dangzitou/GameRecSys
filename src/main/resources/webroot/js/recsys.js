
function appendMovie2Row(rowId, movie, baseUrl) {

    // Normalize data between old Movie model and new Game model
    var id = movie.movieId || movie.appId;
    var title = movie.title || movie.name;
    var imageUrl = movie.headerImage || ('./posters/' + id + '.jpg');
    var ratingValue = movie.positiveReviews || movie.positive || 0;
    
    var year = movie.releaseYear || "";
    if (!year && movie.releaseDate) {
        // Try to extract year from "MMM dd, YYYY" or similar formats
        var parts = movie.releaseDate.split(',');
        if (parts.length > 1) {
            year = parts[parts.length - 1].trim();
        } else {
            year = movie.releaseDate;
        }
    }

    var genresList = [];
    if (Array.isArray(movie.genres)) {
        genresList = movie.genres;
    } else if (typeof movie.genres === 'string') {
        genresList = movie.genres.split(',').map(function(item) { return item.trim(); });
    }

    var genresStr = "";
    $.each(genresList, function(i, genre){
        genresStr += ('<div class="genre"><a href="'+baseUrl+'collection.html?type=genre&value='+genre+'"><b>'+genre+'</b></a></div>');
    });

    var divstr = '<div class="movie-row-item" style="margin-right:5px">\
                    <movie-card-smart>\
                     <movie-card-md1>\
                      <div class="movie-card-md1">\
                       <div class="card">\
                        <link-or-emit>\
                         <a uisref="base.movie" href="./movie.html?movieId='+id+'">\
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
                           <a uisref="base.movie" href="./movie.html?movieId='+id+'">\
                           <span><p class="title">' + title + '</p></span></a>\
                          </link-or-emit>\
                          <div class="rating-indicator">\
                           <ml4-rating-or-prediction>\
                            <div class="rating-or-prediction predicted">\
                             <svg xmlns:xlink="http://www.w3.org/1999/xlink" class="star-icon" height="14px" version="1.1" viewbox="0 0 14 14" width="14px" xmlns="http://www.w3.org/2000/svg">\
                              <defs></defs>\
                              <polygon fill-rule="evenodd" points="13.7714286 5.4939887 9.22142857 4.89188383 7.27142857 0.790044361 5.32142857 4.89188383 0.771428571 5.4939887 4.11428571 8.56096041 3.25071429 13.0202996 7.27142857 10.8282616 11.2921429 13.0202996 10.4285714 8.56096041" stroke="none"></polygon>\
                             </svg>\
                             <div class="rating-value">\
                              '+ratingValue+'\
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
                            <span class="rating-large">'+ratingValue+'</span>\
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
                 <button class="movie-row-back-button" onclick="scrollRow(\'' + rowId + '\', \'left\')">&#10094;</button>\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId +'" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <button class="movie-row-forward-button" onclick="scrollRow(\'' + rowId + '\', \'right\')">&#10095;</button>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
     $(pageId).prepend(divstr);
};

function scrollRow(rowId, direction) {
    var container = document.getElementById(rowId);
    var scrollAmount = 600;
    if (direction === 'left') {
        container.scrollLeft -= scrollAmount;
    } else {
        container.scrollLeft += scrollAmount;
    }
}

function searchGame() {
    var query = document.getElementById('omnisearch-typeahead').value;
    if (query) {
        window.location.href = "collection.html?type=search&value=" + encodeURIComponent(query);
    }
}

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
        if (!movieObject) return;

        // Normalize Data
        var title = movieObject.title || movieObject.name;
        var id = movieObject.movieId || movieObject.appId;
        var description = movieObject.description || movieObject.aboutTheGame;
        var developer = movieObject.developer || movieObject.developers;
        var publisher = movieObject.publisher || movieObject.publishers;
        var positiveReviews = movieObject.positiveReviews || movieObject.positive || 0;
        var headerImage = movieObject.headerImage || ('./posters/' + id + '.jpg');
        var releaseDate = movieObject.releaseDate || 'N/A';
        var price = movieObject.price;

        // Genres Processing
        var genresList = [];
        if (Array.isArray(movieObject.genres)) {
            genresList = movieObject.genres;
        } else if (typeof movieObject.genres === 'string') {
            genresList = movieObject.genres.split(',').map(function(item) { return item.trim(); });
        }

        var genresHtml = "";
        $.each(genresList, function (i, genre) {
            genresHtml += ('<span class="genre-tag"><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '">' + genre + '</a></span>');
        });

        // Media processing
        var mediaItems = [];
        
        // Add videos
        var videoSource = movieObject.productionVideos || movieObject.movies;
        if (videoSource) {
            var vids = videoSource.split(",");
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

        // Main Display (Default to first item or header image)
        var mainDisplayHtml = "";
        if (mediaItems.length > 0) {
            if (mediaItems[0].type === 'video') {
                mainDisplayHtml = '<video id="main-media-video" width="100%" height="100%" controls autoplay muted><source src="' + mediaItems[0].src + '" type="video/mp4">Your browser does not support the video tag.</video><img id="main-media-image" src="" style="width:100%; height:100%; object-fit: contain; display:none;">';
            } else {
                mainDisplayHtml = '<img id="main-media-image" src="' + mediaItems[0].src + '" style="width:100%; height:100%; object-fit: contain;"><video id="main-media-video" width="100%" height="100%" controls style="display:none;"></video>';
            }
        } else {
             mainDisplayHtml = '<img id="main-media-image" src="' + headerImage + '" style="width:100%; height:100%; object-fit: contain;">';
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

        var priceDisplay = price ? "$" + price : "Free";
        if (price === 0 || price === "0") priceDisplay = "Free to Play";

        var languages = movieObject.supportedLanguages || 'N/A';
        if (languages !== 'N/A' && languages.startsWith('[') && languages.endsWith(']')) {
             var content = languages.substring(1, languages.length - 1);
             if (content.trim() === "") {
                 languages = "N/A";
             } else {
                 var parts = content.split(",");
                 languages = parts.map(function(lang) {
                     return lang.trim().replace(/^['"]|['"]$/g, '');
                 }).join(", ");
             }
        }

        var movieDetails = `
            <div class="game-details-container" style="color: #c6d4df; background-color: #1b2838; padding: 20px; max-width: 1200px; margin: 0 auto;">
                <div class="row">
                    <div class="col-md-12">
                        <h1 class="game-title" style="color: #fff; margin-bottom: 20px;">${title}</h1>
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
                            <img src="${headerImage}" style="width: 100%; margin-bottom: 20px;">
                            
                            <div class="info-row">
                                <span class="info-label">Release Date:</span>
                                <span class="info-value">${releaseDate}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Developer:</span>
                                <span class="info-value">${developer || 'N/A'}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Publisher:</span>
                                <span class="info-value">${publisher || 'N/A'}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Price:</span>
                                <span class="info-value price-tag">${priceDisplay}</span>
                            </div>
                             <div class="info-row">
                                <span class="info-label">Reviews:</span>
                                <span class="info-value">${positiveReviews} Positive</span>
                            </div>
                            
                            <div class="info-section" style="margin-top: 20px;">
                                <div class="info-label">Supported Languages:</div>
                                <div class="info-value small-text" style="max-height: 100px; overflow-y: auto;">${languages}</div>
                            </div>
                            
                            <div class="info-section" style="margin-top: 20px;">
                                <div class="info-label">Genres:</div>
                                <div class="genre-list">
                                    ${genresHtml}
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
                                ${description}
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
