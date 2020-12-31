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

            // Enable upload button when a file is selected
            $("#file-upload-input").on("change", function() {
              // Check if the extension is '.csv'
              let file = document.getElementById('file-upload-input').files[0];
              let fileExtension = file.name.split('.').pop();
              var uploadButton = document.getElementById("uploadButtonWS");
              if( fileExtension == 'csv'){
                  uploadButton.disabled = false;
              }else{
                uploadButton.disabled = true;
                alert(fileExtension + " files are not allowed. Try again.");
                document.getElementById("file-upload-input").value = "";
              }
            });

            var stompClient = null;

            // This variable contains all the messages replied from the server
            var generatedCsvContent = [];
            // Counts the number of received messages
            var msgReceived = 0;
            // Num of messages needed from the server to download de client-side generated CSV file
            var msgToReceive = 0;

            function connect() {
                var socket = new SockJS('/ws-uploadCSV');
                socket.binaryType = "arraybuffer";
                stompClient = Stomp.over(socket);
                console.log("Connecting STOMP ...");
                stompClient.connect({}, function (frame) {
                    console.log('Connected: ' + frame);
                    stompClient.subscribe('/user/topic/getCSV',callback);
                    //Client subscribes to /user to receive non-broadcast messages
                });
            }

            // Print received messages from the server
            callback =  function (msg) {
              if (msg.body){
                console.log("Message from server: " + msg.body);
                console.log("msgToReceive: " + msgToReceive);
                console.log("msgReceived: " + msgReceived);

                // Add the message content to the csvArray object
                // The message is converted to array
                let temp = msg.body;
                var processed = document.querySelector('.files');
                // This will return an array with strings "1", "2", etc.
                temp = temp.split(",");
                // Update array content
                generatedCsvContent.push(temp);
                // Check if all messages has been received
                if (msgReceived == msgToReceive){
                    processed.setAttribute('data-before', 'Upload complete!');
                    msgReceived = 0;
                    msgToReceive = 0;
                    download();
                }
                else{
                    //Shows on screen the % processed
                    var percent_done = Math.floor( ( msgReceived / msgToReceive ) * 100 );
                    processed.setAttribute('data-before', `Proccesing File -  ${percent_done}% ...`);
                    msgReceived ++;
                }
              }else{
                console.log("Empty msg.");
              }
             }


            // Download the client-side generated CSV file
            // Source: https://stackoverflow.com/questions/14964035/how-to-export-javascript-array-info-to-csv-on-client-side
            function download() {
                console.log("Downloading CSV file ...");
                // Split the content of the array of messages
                let csvContent = "data:text/csv;charset=utf-8,"
                    + generatedCsvContent.map(e => e.join(",")).join("\n");
                var encodedUri = encodeURI(csvContent);
                var link = document.createElement("a");
                link.setAttribute("href", encodedUri);
                link.setAttribute("download", "shorted-urls.csv");
                document.body.appendChild(link);
                link.click(); // This will download the data file named "my_data.csv".
            }

            // Source: https://www.sitepoint.com/delay-sleep-pause-wait/
            function sleep(ms) {
              return new Promise(resolve => setTimeout(resolve, ms));
            }

            var connectButton = document.getElementById("uploadButtonWS");
            connectButton.onclick = function() {
                connect()
                // We need to sleep before the connection is established
                sleep(1000).then(() =>{
                    sendFileWS()
                });
            };

            function sendFileWS(){
                var file = document.getElementById('file-upload-input').files[0];

                // Number of lines to send per message
                const slice_size = 20;

                var reader = new FileReader();
                var rawData = new ArrayBuffer();

                reader.onloadend = function( event ) {
                    if ( event.target.readyState !== FileReader.DONE ) {
                        return;
                    }
                    rawData = event.target.result;
                    // Split csv content on an array
                    var content = rawData.split(/(?:\r\n|\n)+/);
                    // Obtain the number of messages from the server to receive
                    msgToReceive = content.length - 1;
                    upload_file( 0 );

                    function upload_file( start ){
                        var next_slice = start + slice_size + 1;
                        //var blob = file.slice( start, next_slice );
                        var lines = content.slice(start, next_slice);

                        stompClient.send("/app/uploadCSV", {}, lines);
                        console.log("Part transfered. Size: " + next_slice );
                        // Once transferred a part, check if there are more content to send

                        if ( next_slice < content.length ) {
                            // More to upload, call function recursively
                            upload_file( next_slice );
                        }
                    }
                }
                reader.readAsText(file);
            }
    });