ACC.novalnetplaceorder = {

    paymentOption: '',

    bindAll: function () {

        this.bindPlaceOrder();

    },

    bindPlaceOrder: function () {

        const PLACE_ORDER = "#placeOrder";

        $("#Terms1").on("click", function () {

            if ($("#Terms1").prop("checked")) {

                $(PLACE_ORDER).removeAttr("disabled");

            } else {

                $(PLACE_ORDER).attr('disabled', 'disabled');

            }

        });

        $(PLACE_ORDER).on("click", function () {

            if (!$("#Terms1").prop("checked")) {

                if (!$("#termserror").length) {

                    const errorMessage = $("#termsCheckErrorMessage").val();
                    const div = $("<div>")
                        .attr("id", "termserror")
                        .attr("class", "alert alert-danger alert-dismissable getAccAlert")
                        .text(errorMessage);

                    $("#placeOrderForm1").prepend(div);

                }

                return false;

            } else {

                $(PLACE_ORDER).attr("disabled", "disabled");
                $("#placeOrderForm1").submit();

                return true;

            }

        });

    },

    updatePlaceOrderButton: function () {

        const PLACE_ORDER = "#placeOrder";

        $(PLACE_ORDER).removeAttr("disabled");
    }

};

$(document).ready(function () {

    ACC.novalnetplaceorder.bindAll();

});

