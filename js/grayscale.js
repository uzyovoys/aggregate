/*!
 * Start Bootstrap - Grayscale Bootstrap Theme (http://startbootstrap.com)
 * Code licensed under the Apache License v2.0.
 * For details, see http://www.apache.org/licenses/LICENSE-2.0.
 */

// jQuery to collapse the navbar on scroll
var fadeDivs$ = $('.fadeInScroll');
var $win = $(window);
$win.scroll(function() {
    if ($(".navbar").offset().top > 50) {
        $(".navbar-fixed-top").addClass("top-nav-collapse");
    } else {
        $(".navbar-fixed-top").removeClass("top-nav-collapse");
    }
    fadeIn();
});

// jQuery for page scrolling feature - requires jQuery Easing plugin
$(function() {
    $('a.page-scroll').bind('click', function(event) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: $($anchor.attr('href')).offset().top
        }, 1500, 'easeInOutExpo');
        event.preventDefault();
    });
    $(".various").fancybox({
      maxWidth    : 800,
      maxHeight   : 600,
      fitToView   : false,
      width       : '70%',
      height      : '70%',
      autoSize    : false,
      closeClick  : false,
      openEffect  : 'none',
      closeEffect : 'none'
    });
    fadeDivs$.fadeTo(0, 0);
    fadeIn();
});

function fadeIn() {
    var b = $win.scrollTop() + $win.height();
    fadeDivs$.each(function () {
        var a = $(this).offset().top + $(this).height();
        if (a < b) $(this).fadeTo(500,1);
    });
}

// Closes the Responsive Menu on Menu Item Click
$('.navbar-collapse ul li a').click(function() {
    $('.navbar-toggle:visible').click();
});