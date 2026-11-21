$(function () {
    // Shared frontend enhancements. Keep validation lightweight; server validation is authoritative.
    $("form").on("submit", function () {
        $(this).find("button[type='submit'], button:not([type])").prop("disabled", true);
    });
});
