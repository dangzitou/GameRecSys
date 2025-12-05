
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
        genresList = movie.genres.split(',').map(function (item) { return item.trim(); });
    }

    var genresStr = "";
    $.each(genresList, function (i, genre) {
        genresStr += ('<div class="genre"><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '"><b>' + genre + '</b></a></div>');
    });

    var divstr = '<div class="movie-row-item" style="margin-right:5px">\
                    <movie-card-smart>\
                     <movie-card-md1>\
                      <div class="movie-card-md1">\
                       <div class="card">\
                        <link-or-emit>\
                         <a uisref="base.movie" href="./game.html?id='+ id + '">\
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
                           <a uisref="base.movie" href="./game.html?id='+ id + '">\
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
                              '+ ratingValue + '\
                             </div>\
                            </div>\
                           </ml4-rating-or-prediction>\
                          </div>\
                          <p class="year">'+ year + '</p>\
                         </div>\
                         <div class="below-fold">\
                          <div class="genre-list">\
                           '+ genresStr + '\
                          </div>\
                          <div class="ratings-display">\
                           <div class="rating-average">\
                            <span class="rating-large">'+ ratingValue + '</span>\
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
    $('#' + rowId).append(divstr);
};


function addRowFrame(pageId, rowName, rowId, baseUrl) {
    var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <a class="plainlink" title="go to the full list" href="'+ baseUrl + 'collection.html?type=genre&value=' + rowName + '">' + rowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <button class="movie-row-back-button" onclick="scrollRow(\'' + rowId + '\', \'left\')">&#10094;</button>\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId + '" style="margin-left: 0px;">\
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
                 <a class="plainlink" title="go to the full list" href="'+ baseUrl + 'collection.html?type=genre&value=' + rowName + '">' + rowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
    $(pageId).prepend(divstr);
};

function addGenreRow(pageId, rowName, rowId, size, baseUrl) {
    addRowFrame(pageId, rowName, rowId, baseUrl);
    $.getJSON(baseUrl + "getrecommendation?genre=" + rowName + "&size=" + size + "&sortby=positiveReviews", function (result) {
        // Handle new response format {games: [...], totalPages: ...}
        var games = result.games || result; // Fallback if backend not updated yet
        $.each(games, function (i, movie) {
            appendMovie2Row(rowId, movie, baseUrl);
        });
    });
};

function addGenrePage(pageId, rowName, rowId, page, size, baseUrl) {
    var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <a class="plainlink" title="go to the full list" href="'+ baseUrl + 'collection.html?type=genre&value=' + rowName + '">' + rowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId + '" style="margin-left: 0px; white-space: normal;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
                <div class="pagination-container" style="text-align: center; margin-top: 20px; padding-bottom: 20px;">\
                    <button class="btn btn-default" onclick="changePage(\''+ rowName + '\', ' + (page - 1) + ')" ' + (page <= 1 ? 'disabled' : '') + '>Previous</button>\
                    <span style="margin: 0 15px; font-weight: bold; color: #fff;">Page '+ page + '</span>\
                    <button class="btn btn-default" id="next-page-btn" onclick="changePage(\''+ rowName + '\', ' + (page + 1) + ')">Next</button>\
                </div>\
               </div>';

    $(pageId).html(divstr);

    $.getJSON(baseUrl + "getrecommendation?genre=" + rowName + "&size=" + size + "&page=" + page + "&sortby=positiveReviews", function (result) {
        var games = result.games;
        var totalPages = result.totalPages;
        var currentPage = result.currentPage;

        $.each(games, function (i, movie) {
            appendMovie2Row(rowId, movie, baseUrl);
        });

        // Update pagination controls
        var paginationHtml = '';
        paginationHtml += '<button class="btn btn-default" onclick="changePage(\'' + rowName + '\', ' + (currentPage - 1) + ')" ' + (currentPage <= 1 ? 'disabled' : '') + '>Previous</button>';
        paginationHtml += '<span style="margin: 0 15px; font-weight: bold; color: #fff;">Page ' + currentPage + ' of ' + totalPages + '</span>';
        paginationHtml += '<button class="btn btn-default" onclick="changePage(\'' + rowName + '\', ' + (currentPage + 1) + ')" ' + (currentPage >= totalPages ? 'disabled' : '') + '>Next</button>';

        // Jump to page
        paginationHtml += '<span style="margin-left: 20px; color: #ccc;">Go to: </span>';
        paginationHtml += '<input type="number" id="jump-input-' + rowId + '" min="1" max="' + totalPages + '" style="width: 60px; color: #000; text-align: center;" value="' + currentPage + '">';
        paginationHtml += '<button class="btn btn-default btn-sm" onclick="jumpToPage(\'' + rowName + '\', \'' + rowId + '\')">Go</button>';

        $(pageId).find('.pagination-container').html(paginationHtml);
    });
}

function jumpToPage(genre, rowId) {
    var inputVal = $('#jump-input-' + rowId).val();
    var page = parseInt(inputVal);
    if (page && page > 0) {
        changePage(genre, page);
    }
}

function changePage(genre, newPage) {
    if (newPage < 1) return;
    window.location.href = "collection.html?type=genre&value=" + genre + "&page=" + newPage;
}



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
        var negativeReviews = movieObject.negativeReviews || movieObject.negative || 0;
        var headerImage = movieObject.headerImage || ('./posters/' + id + '.jpg');
        var releaseDate = movieObject.releaseDate || 'N/A';
        var price = movieObject.price;

        // New fields
        var averagePlaytime = movieObject.averagePlaytimeForever || 0;
        var medianPlaytime = movieObject.medianPlaytimeForever || 0;
        var achievements = movieObject.achievements || 0;
        var recommendations = movieObject.recommendations || 0;
        var metacriticScore = movieObject.metacriticScore || 0;
        var categories = movieObject.categories || '';

        // Calculate positive rate
        var totalReviews = positiveReviews + negativeReviews;
        var positiveRate = totalReviews > 0 ? Math.round((positiveReviews / totalReviews) * 100) : 0;

        // Determine review status
        var reviewStatus = "No Reviews";
        var reviewStatusClass = "review-status-none";
        if (totalReviews > 0) {
            if (positiveRate >= 95) { reviewStatus = "Overwhelmingly Positive"; reviewStatusClass = "review-status-positive"; }
            else if (positiveRate >= 80) { reviewStatus = "Very Positive"; reviewStatusClass = "review-status-positive"; }
            else if (positiveRate >= 70) { reviewStatus = "Mostly Positive"; reviewStatusClass = "review-status-positive"; }
            else if (positiveRate >= 40) { reviewStatus = "Mixed"; reviewStatusClass = "review-status-mixed"; }
            else if (positiveRate >= 20) { reviewStatus = "Mostly Negative"; reviewStatusClass = "review-status-negative"; }
            else { reviewStatus = "Overwhelmingly Negative"; reviewStatusClass = "review-status-negative"; }
        }

        // Format playtime (minutes to hours)
        var playtimeHours = (averagePlaytime / 60).toFixed(1);
        var medianPlaytimeHours = (medianPlaytime / 60).toFixed(1);

        // Genres Processing
        var genresList = [];
        if (Array.isArray(movieObject.genres)) {
            genresList = movieObject.genres;
        } else if (typeof movieObject.genres === 'string') {
            genresList = movieObject.genres.split(',').map(function (item) { return item.trim(); });
        }

        var genresHtml = "";
        $.each(genresList, function (i, genre) {
            genresHtml += ('<span class="genre-tag"><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '">' + genre + '</a></span>');
        });

        // Categories Processing
        var categoriesList = [];
        if (typeof categories === 'string' && categories.trim() !== '') {
            categoriesList = categories.split(',').map(function (item) { return item.trim(); });
        }
        var categoriesHtml = "";
        $.each(categoriesList, function (i, cat) {
            categoriesHtml += '<span class="category-tag">' + cat + '</span>';
        });

        // Media processing
        var mediaItems = [];

        // Add videos
        var videoSource = movieObject.productionVideos || movieObject.movies;
        if (videoSource) {
            var vids = videoSource.split(",");
            $.each(vids, function (i, vid) {
                if (vid.trim() !== "") {
                    mediaItems.push({ type: 'video', src: vid.trim() });
                }
            });
        }

        // Add screenshots
        if (movieObject.screenshots) {
            var shots = movieObject.screenshots.split(",");
            $.each(shots, function (i, shot) {
                if (shot.trim() !== "") {
                    mediaItems.push({ type: 'image', src: shot.trim() });
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
        $.each(mediaItems, function (i, item) {
            if (item.type === 'video') {
                sliderHtml += '<div class="slider-item" onclick="changeMainMedia(\'video\', \'' + item.src + '\')"><video src="' + item.src + '" style="width:100%; height:100%; object-fit: cover;" muted></video></div>';
            } else {
                sliderHtml += '<div class="slider-item" onclick="changeMainMedia(\'image\', \'' + item.src + '\')"><img src="' + item.src + '" style="width:100%; height:100%; object-fit: cover;"></div>';
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
                languages = parts.map(function (lang) {
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
                    <!-- Left Column: Media + Description -->
                    <div class="col-md-8">
                        <div class="main-media-container" style="background: #000; height: 400px; display: flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                            ${mainDisplayHtml}
                        </div>
                        <div class="media-slider" style="display: flex; overflow-x: auto; gap: 5px; padding-bottom: 10px;">
                            ${sliderHtml}
                        </div>

                        <!-- Description Section - Now inside left column -->
                        <div class="game-description" style="margin-top: 20px;">
                            <h3 style="color: #fff; border-bottom: 1px solid #3a4b5c; padding-bottom: 10px;">About This Game</h3>
                            <div style="font-size: 14px; line-height: 1.6; color: #acb2b8;">
                                ${description}
                            </div>
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

                            <!-- Reviews Section - Enhanced -->
                            <div class="info-row" style="margin-top: 15px; padding: 10px; background: rgba(0,0,0,0.2); border-radius: 4px;">
                                <div style="margin-bottom: 8px;">
                                    <span class="info-label">User Reviews:</span>
                                    <span class="${reviewStatusClass}" style="font-weight: bold;">${reviewStatus}</span>
                                </div>
                                <div style="display: flex; gap: 15px; font-size: 13px;">
                                    <span style="color: #66c0f4;">Positive: ${positiveReviews.toLocaleString()}</span>
                                    <span style="color: #a34c25;">Negative: ${negativeReviews.toLocaleString()}</span>
                                    <span style="color: #8f98a0;">(${positiveRate}%)</span>
                                </div>
                            </div>

                            <!-- Playtime Stats -->
                            <div class="info-row" style="margin-top: 10px;">
                                <span class="info-label">Avg Playtime:</span>
                                <span class="info-value">${playtimeHours} hours</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">Median Playtime:</span>
                                <span class="info-value">${medianPlaytimeHours} hours</span>
                            </div>

                            <!-- Additional Stats -->
                            ${metacriticScore > 0 ? `
                            <div class="info-row">
                                <span class="info-label">Metacritic:</span>
                                <span class="info-value metacritic-score" style="background: ${metacriticScore >= 75 ? '#66cc33' : metacriticScore >= 50 ? '#ffcc33' : '#ff0000'}; color: #fff; padding: 2px 8px; border-radius: 3px; font-weight: bold;">${metacriticScore}</span>
                            </div>` : ''}
                            ${achievements > 0 ? `
                            <div class="info-row">
                                <span class="info-label">Achievements:</span>
                                <span class="info-value">${achievements}</span>
                            </div>` : ''}
                            ${recommendations > 0 ? `
                            <div class="info-row">
                                <span class="info-label">Recommendations:</span>
                                <span class="info-value">${recommendations.toLocaleString()}</span>
                            </div>` : ''}

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

                            ${categoriesHtml ? `
                            <div class="info-section" style="margin-top: 15px;">
                                <div class="info-label">Categories:</div>
                                <div class="category-list" style="margin-top: 5px;">
                                    ${categoriesHtml}
                                </div>
                            </div>` : ''}

                            <!-- 用户评分区域 -->
                            <div class="user-rating-section" id="user-rating-section" data-game-id="${id}">
                                <div class="info-label">Rate This Game:</div>
                                <div class="star-rating-container">
                                    <div class="star-rating" id="star-rating">
                                        <span class="star" data-rating="1">★</span>
                                        <span class="star" data-rating="2">★</span>
                                        <span class="star" data-rating="3">★</span>
                                        <span class="star" data-rating="4">★</span>
                                        <span class="star" data-rating="5">★</span>
                                    </div>
                                    <span class="rating-text" id="rating-text">Click to rate</span>
                                </div>
                                <div class="rating-stats" id="rating-stats">
                                    <span class="avg-rating">Avg: <span id="avg-rating-value">--</span></span>
                                    <span class="rating-count">(<span id="rating-count-value">0</span> ratings)</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        $("#" + containerId).prepend(movieDetails);

        // 初始化评分功能
        initRatingSystem(id);
    });
};

// Helper function for media switching
window.changeMainMedia = function (type, src) {
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

$(document).ready(function () {
    // Bind search button click
    $('.base-header-search-box button').on('click', function (e) {
        e.preventDefault();
        searchGame();
    });

    // Bind enter key in search input
    $('#omnisearch-typeahead').on('keypress', function (e) {
        if (e.which === 13) {
            e.preventDefault();
            searchGame();
        }
    });

    // Check login status on page load
    checkLoginStatus();

    // Close dropdown when clicking outside
    $(document).on('click', function (e) {
        if (!$(e.target).closest('.user-menu-container').length) {
            $('#userDropdown').removeClass('show');
        }
    });
});

// ========== Auth Functions ==========

var API_BASE_URL = window.location.protocol + "//" + window.location.host;

// Check if user is logged in
function checkLoginStatus() {
    var token = localStorage.getItem('authToken');
    var userInfo = localStorage.getItem('userInfo');

    if (token && userInfo) {
        try {
            var user = JSON.parse(userInfo);
            showLoggedInState(user);
        } catch (e) {
            handleLogout();
        }
    } else {
        showGuestState();
    }
}

// Show logged in state
function showLoggedInState(user) {
    $('#guestAuthButtons').addClass('hidden');
    $('#userMenuContainer').addClass('logged-in');

    var displayName = user.nickname || user.username || '用户';
    $('#userName').text(displayName);
    $('#userAvatar').text(displayName.charAt(0).toUpperCase());
}

// Show guest state
function showGuestState() {
    $('#guestAuthButtons').removeClass('hidden');
    $('#userMenuContainer').removeClass('logged-in');
}

// Toggle user dropdown
function toggleUserDropdown() {
    $('#userDropdown').toggleClass('show');
}

// Open auth modal
function openAuthModal(tab) {
    $('#authModalOverlay').addClass('show');
    switchAuthTab(tab || 'login');
    hideAuthMessage();
}

// Close auth modal
function closeAuthModal() {
    $('#authModalOverlay').removeClass('show');
    // Reset forms
    $('#loginForm')[0].reset();
    $('#registerForm')[0].reset();
    hideAuthMessage();
}

// Close modal when clicking overlay
function closeAuthModalOnOverlay(event) {
    if (event.target.id === 'authModalOverlay') {
        closeAuthModal();
    }
}

// Switch between login and register tabs
function switchAuthTab(tab) {
    hideAuthMessage();

    if (tab === 'login') {
        $('#loginTab').addClass('active');
        $('#registerTab').removeClass('active');
        $('#loginForm').addClass('active');
        $('#registerForm').removeClass('active');
        $('#authModalTitle').text('登录');
    } else {
        $('#loginTab').removeClass('active');
        $('#registerTab').addClass('active');
        $('#loginForm').removeClass('active');
        $('#registerForm').addClass('active');
        $('#authModalTitle').text('注册');
    }
}

// Show auth message
function showAuthMessage(message, type) {
    var $msg = $('#authMessage');
    $msg.text(message);
    $msg.removeClass('success error').addClass(type).addClass('show');
}

// Hide auth message
function hideAuthMessage() {
    $('#authMessage').removeClass('show');
}

// Handle login
function handleLogin(event) {
    event.preventDefault();

    var username = $('#loginUsername').val().trim();
    var password = $('#loginPassword').val();

    if (!username || !password) {
        showAuthMessage('请填写用户名和密码', 'error');
        return;
    }

    $('#loginSubmitBtn').prop('disabled', true).text('登录中...');

    $.ajax({
        url: API_BASE_URL + '/auth/login',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            password: password
        }),
        success: function (response) {
            if (response.success) {
                // Save token and user info
                localStorage.setItem('authToken', response.token);
                localStorage.setItem('userInfo', JSON.stringify(response.user));

                showAuthMessage('登录成功！', 'success');

                setTimeout(function () {
                    closeAuthModal();
                    showLoggedInState(response.user);
                }, 1000);
            } else {
                showAuthMessage(response.message || '登录失败', 'error');
            }
        },
        error: function (xhr) {
            var msg = '登录失败，请稍后重试';
            try {
                var resp = JSON.parse(xhr.responseText);
                if (resp.message) msg = resp.message;
            } catch (e) { }
            showAuthMessage(msg, 'error');
        },
        complete: function () {
            $('#loginSubmitBtn').prop('disabled', false).text('登录');
        }
    });
}

// Handle register
function handleRegister(event) {
    event.preventDefault();

    var username = $('#registerUsername').val().trim();
    var email = $('#registerEmail').val().trim();
    var nickname = $('#registerNickname').val().trim();
    var password = $('#registerPassword').val();
    var confirmPassword = $('#registerConfirmPassword').val();

    if (!username) {
        showAuthMessage('请填写用户名', 'error');
        return;
    }

    if (password.length < 6) {
        showAuthMessage('密码长度不能少于6位', 'error');
        return;
    }

    if (password !== confirmPassword) {
        showAuthMessage('两次输入的密码不一致', 'error');
        return;
    }

    $('#registerSubmitBtn').prop('disabled', true).text('注册中...');

    $.ajax({
        url: API_BASE_URL + '/auth/register',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            password: password,
            email: email || null,
            nickname: nickname || null
        }),
        success: function (response) {
            if (response.success) {
                showAuthMessage('注册成功！请登录', 'success');

                setTimeout(function () {
                    switchAuthTab('login');
                    $('#loginUsername').val(username);
                }, 1500);
            } else {
                showAuthMessage(response.message || '注册失败', 'error');
            }
        },
        error: function (xhr) {
            var msg = '注册失败，请稍后重试';
            try {
                var resp = JSON.parse(xhr.responseText);
                if (resp.message) msg = resp.message;
            } catch (e) { }
            showAuthMessage(msg, 'error');
        },
        complete: function () {
            $('#registerSubmitBtn').prop('disabled', false).text('注册');
        }
    });
}

// Handle logout
function handleLogout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
    $('#userDropdown').removeClass('show');
    showGuestState();
}

// Get auth token for API calls
function getAuthToken() {
    return localStorage.getItem('authToken');
}

// Make authenticated API call
function authFetch(url, options) {
    options = options || {};
    options.headers = options.headers || {};

    var token = getAuthToken();
    if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
    }

    return $.ajax(url, options);
}

// ========== 用户评分功能 ==========

/**
 * 初始化评分系统
 */
function initRatingSystem(gameId) {
    // 获取当前评分数据
    loadRatingData(gameId);

    // 绑定星星点击事件
    $('#star-rating .star').on('click', function () {
        var rating = $(this).data('rating');
        submitRating(gameId, rating);
    });

    // 绑定星星悬浮效果
    $('#star-rating .star').on('mouseenter', function () {
        var rating = $(this).data('rating');
        highlightStars(rating);
        $('#rating-text').text(getRatingText(rating));
    });

    $('#star-rating').on('mouseleave', function () {
        var currentRating = $('#star-rating').data('user-rating') || 0;
        highlightStars(currentRating);
        if (currentRating > 0) {
            $('#rating-text').text('Your rating: ' + currentRating);
        } else {
            $('#rating-text').text('Click to rate');
        }
    });
}

/**
 * 加载评分数据
 */
function loadRatingData(gameId) {
    var token = getAuthToken();
    var headers = {};
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    $.ajax({
        url: '/rating?gameId=' + gameId,
        method: 'GET',
        headers: headers,
        success: function (response) {
            if (response.success) {
                updateRatingDisplay(response);
            }
        },
        error: function (xhr, status, error) {
            console.error('Failed to load rating data:', error);
        }
    });
}

/**
 * 更新评分显示
 */
function updateRatingDisplay(data) {
    // 更新平均评分和评分数量
    var avgRating = data.averageRating || 0;
    var ratingCount = data.ratingCount || 0;

    $('#avg-rating-value').text(avgRating.toFixed(1));
    $('#rating-count-value').text(ratingCount);

    // 更新用户评分
    if (data.userRating) {
        var userRating = data.userRating;
        $('#star-rating').data('user-rating', userRating);
        highlightStars(userRating);
        $('#rating-text').text('Your rating: ' + userRating);
    }
}

/**
 * 提交评分
 */
function submitRating(gameId, rating) {
    var token = getAuthToken();

    if (!token) {
        alert('请先登录后再评分');
        openAuthModal('login');
        return;
    }

    $.ajax({
        url: '/rating',
        method: 'POST',
        contentType: 'application/json',
        headers: {
            'Authorization': 'Bearer ' + token
        },
        data: JSON.stringify({
            gameId: gameId,
            rating: rating
        }),
        success: function (response) {
            if (response.success) {
                $('#star-rating').data('user-rating', rating);
                highlightStars(rating);
                $('#rating-text').text('Your rating: ' + rating);
                $('#avg-rating-value').text(response.averageRating.toFixed(1));
                $('#rating-count-value').text(response.ratingCount);

                // 显示成功提示
                showRatingToast('Rating submitted successfully!');
            } else {
                alert(response.message || 'Rating failed');
            }
        },
        error: function (xhr, status, error) {
            console.error('Rating failed:', error);
            if (xhr.status === 401) {
                alert('Please login first');
                openAuthModal('login');
            } else {
                alert('Rating failed, please try again');
            }
        }
    });
}

/**
 * 高亮星星
 */
function highlightStars(rating) {
    $('#star-rating .star').each(function () {
        var starRating = $(this).data('rating');
        if (starRating <= rating) {
            $(this).addClass('active');
        } else {
            $(this).removeClass('active');
        }
    });
}

/**
 * 获取评分文本
 */
function getRatingText(rating) {
    var texts = {
        1: 'Poor',
        2: 'Fair',
        3: 'Good',
        4: 'Very Good',
        5: 'Excellent'
    };
    return texts[rating] || '';
}

/**
 * 显示评分提示
 */
function showRatingToast(message) {
    var toast = $('<div class="rating-toast">' + message + '</div>');
    $('body').append(toast);

    setTimeout(function () {
        toast.addClass('show');
    }, 10);

    setTimeout(function () {
        toast.removeClass('show');
        setTimeout(function () {
            toast.remove();
        }, 300);
    }, 2000);
}
