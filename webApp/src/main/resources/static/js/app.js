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
                            "<div class='alert alert-success lead'><a target='_blank' href='"
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
         //Tutorial: https://attacomsian.com/blog/spring-boot-file-upload-with-ajax
        $("#file-upload-form").on("submit", function(e){
            e.preventDefault();
            $.ajax({
                url: "/uploadCSV",
                type: "POST",
                data: new FormData(this),
                //enctype: 'multipart/form-data',
                processData: false,
                contentType: false,
                cache: false,
                success: function(data){
                    console.log("RESULTADO:");
                    console.log(data);
                    $.ajax({
                        url: "/files/"+data,
                        type:"GET",
                        success: function(res){
                            var blob = new Blob([res]);
                            var link=document.createElement('a');
                            link.href=window.URL.createObjectURL(blob);
                            link.download="shorted_urls.csv";
                            link.click();
                        }
                    });
                },
                error: function(err) {
                    console.error(err);
                }
            });
        });
    });