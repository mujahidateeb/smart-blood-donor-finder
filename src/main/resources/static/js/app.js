function switchDashboardTab(tabName) {
    if (!tabName) return;
    $(".tab-button").removeClass("active");
    $(".tab-pane").removeClass("active");
    $("#" + tabName + "-tab-btn").addClass("active");
    $("#" + tabName + "-tab-content").addClass("active");

    if (window.history && window.history.replaceState) {
        var newUrl = window.location.pathname + "?tab=" + tabName;
        window.history.replaceState({ tab: tabName }, "", newUrl);
    }
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
        $(this).find("button[type='submit'], button:not([type])").prop("disabled", true);
    });

    // Check URL search param or hash for active tab
    var urlParams = new URLSearchParams(window.location.search);
    var tab = urlParams.get("tab");
    if (!tab && window.location.hash) {
        tab = window.location.hash.replace("#", "");
    }
    if (tab && $("#" + tab + "-tab-btn").length) {
        switchDashboardTab(tab);
    }

    // Role toggle on profile page
    $("#role-donor").on("change", function () {
        updateDonorAvailabilityVisibility();
    });
});
