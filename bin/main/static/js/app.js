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
<<<<<<< Updated upstream
                            + "</a><p>Safeness: " + msg.safe + "</p>"
//                            + "<img src=\"data:image/png;base64, " + msg.qrCode + "\" />"
                            + "<img src=\"" + msg.qrCode + "\" />"
                            + "</div>" );
=======
                            + "</a></div>");
>>>>>>> Stashed changes
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });