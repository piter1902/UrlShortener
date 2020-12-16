$(document).ready(
    function () {
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/link",
                    data: $(this).serialize(),
                    success: function (msg) {
                        $("#result").html(
                            "<div class='alert alert-success lead '><a target='_blank' href='"
                            + msg.uri
                            + "'>"
                            + msg.uri
                            + "</a><p>Safeness: " + msg.safe + "</p>"
//                            + "<img src=\"data:image/png;base64, " + msg.qrCode + "\" />"
                            + "<img src=\"" + msg.qrCode + "\" />"
                            + "</div>" );
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
            // Add the following code if you want the name of the file appear on select
            $(".custom-file-input").on("change", function() {
              var fileName = $(this).val().split("\\").pop();
              $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
              var uploadButton = document.getElementById("uploadButton");
              uploadButton.disabled = false;
            });
            // Tutorial https://www.accelebrate.com/blog/web-page-file-uploads-ajax-part-2-of-3/
            $("#file-upload-form").on("submit", function(e){
                e.preventDefault();
                // Create a new FormData object to hold the file datavar
                fd = new FormData();
                // Add the data from the file input field to the FormData object
                fd.append("file", document.getElementById("file-upload-input").files[0]);
                // Initialize a new XHR object
                var xhr = new XMLHttpRequest();
                // Progress bar object
                var progressBar = document.getElementById("progress")
                // handle ready state change events
                xhr.onreadystatechange = function() {

                  if (xhr.readyState === 4 && xhr.status === 201) {
                    // upload is complete, output result data
                    console.log(xhr.responseText);
                    progressBar.value = 0;
                    downloadFile(xhr.responseText);
                  }
                };

                // configure the request
                xhr.open("POST", "/uploadCSV");
                // Show progress bar while uploading file
                xhr.upload.onprogress = function (e) {
                    if (e.lengthComputable) {
                        progressBar.max = e.total;
                        progressBar.value = e.loaded;
                    }
                }
                xhr.upload.onloadstart = function (e) {
                    progressBar.value = 0;
                }
                xhr.upload.onloadend = function (e) {
                    progressBar.value = e.loaded;
                }

                  // makes the request and uploads the form data
                  xhr.send(fd);
            });

            function downloadFile(fileName){
                var xhr = new XMLHttpRequest();
                xhr.onreadystatechange = function() {
                    if (this.readyState == 4 && this.status == 200) {
                        var blob = new Blob([xhr.response]);
                        var link=document.createElement('a');
                        link.href=window.URL.createObjectURL(blob);
                        link.download="shorted_urls.csv";
                        link.click();
                    }
                  };
                  xhr.open("GET", "/files/"+fileName, true);
                  xhr.send();
            }
        });