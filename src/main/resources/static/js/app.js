function switchDashboardTab(tabName) {
    if (!tabName) return;
    window.location.href = "/dashboard?tab=" + encodeURIComponent(tabName);
}

function updateDonorAvailabilityVisibility() {
    if ($("#role-donor").length) {
        if ($("#role-donor").is(":checked")) {
            $("#donorAvailabilitySection").slideDown(200);
        } else {
            $("#donorAvailabilitySection").slideUp(200);
        }
    }
}

$(function () {
    // Shared frontend enhancements. Keep validation lightweight; server validation is authoritative.
    $("form").on("submit", function () {
        var $form = $(this);
        setTimeout(function () {
            $form.find("button[type='submit'], button:not([type])").prop("disabled", true);
        }, 0);
    });

    // Role toggle on profile page
    $("#role-donor").on("change", function () {
        updateDonorAvailabilityVisibility();
    });
});
