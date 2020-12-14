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

            ///////////////WebSocket functionality//////////////////
            // Source: https://stackoverflow.com/questions/27959052/send-a-file-using-websocket-javascript-client
            // Source split file: https://deliciousbrains.com/using-javascript-file-api-to-avoid-file-upload-limits/

            var stompClient = null;
            var connectButton = document.getElementById("connectWS");
            connectButton.onclick = function() {connect()};

            function connect() {
                var socket = new SockJS('/ws-uploadCSV');
                socket.binaryType = "arraybuffer";
                stompClient = Stomp.over(socket);
                console.log("Connecting STOMP ...");
                stompClient.connect({}, function (frame) {
                    console.log('Connected: ' + frame);
                    stompClient.subscribe('/topic/getCSV',callback);
                });
            }

            // Print received messages from the server
            callback =  function (msg) {
              if (msg.body){
                console.log("Message from server: " + msg.body);
              }else{
                console.log("Empty msg.")
              }
             }

            var connectButton = document.getElementById("uploadButtonWS");
            connectButton.onclick = function() {sendFileWS()};

            function sendFileWS(){
                var file = document.getElementById('file-upload-input').files[0];
                //console.log("File content: " + file);
                var slice_size = 1024;

                var reader = new FileReader();
                var rawData = new ArrayBuffer();
                upload_file( 0 );

                function upload_file( start ){
                    var next_slice = start + slice_size + 1;
                    var blob = file.slice( start, next_slice );
                    reader.onloadend = function( event ) {
                        if ( event.target.readyState !== FileReader.DONE ) {
                            return;
                        }
                        rawData = event.target.result;
                        //console.log("Sending: " + rawData);
                        // Poner cabecera de que es de texto
                        stompClient.send("/app/uploadCSV", {}, rawData);
                        console.log("Part transfered. Size: " + next_slice );
                        // Once tranfered a part, check if there are more content to send
                        var size_done = start + slice_size;
                        var percent_done = Math.floor( ( size_done / file.size ) * 100 );

                        if ( next_slice < file.size ) {
                            // Update upload progress
                            $( '#dbi-upload-progress' ).html( `Uploading File -  ${percent_done}%` );

                            // More to upload, call function recursively
                            upload_file( next_slice );
                        } else {
                            // Update upload progress
                            $( '#dbi-upload-progress' ).html( 'Upload Complete!' );
                        }
                    }
                    reader.readAsText(blob);
                }
//                var headers = {};
//                headers["content-type"] = "text/plain";

            }

         //Tutorial: https://attacomsian.com/blog/spring-boot-file-upload-with-ajax
//        $("#file-upload-form").on("submit", function(e){
//            e.preventDefault();
//            $.ajax({
//                url: "/uploadCSV",
//                type: "POST",
//                data: new FormData(this),
//                //enctype: 'multipart/form-data',
//                processData: false,
//                contentType: false,
//                cache: false,
//                success: function(data){
//                    console.log("RESULTADO:");
//                    console.log(data);
//                    $.ajax({
//                        url: "/files/"+data,
//                        type:"GET",
//                        success: function(res){
//                            var blob = new Blob([res]);
//                            var link=document.createElement('a');
//                            link.href=window.URL.createObjectURL(blob);
//                            link.download="shorted_urls.csv";
//                            link.click();
//                        }
//                    });
//                },
//                error: function(err) {
//                    console.error(err);
//                }
//            });
//        });
    });