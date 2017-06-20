/**
 * Gulp前端管理任务
 *
 * @author huchiwei
 * @create 2016-09-06
 */

var gulp = require('gulp');
var $ = require("gulp-load-plugins")();
var combiner = require('stream-combiner2');
var browserSync = require('browser-sync');
var del = require('del');
var babel = require('gulp-babel');
var runSequence = require("run-sequence");


/* === 基本路径及输出目录 === */
var _baseUrl = "src/main/assets",
    _dist = "src/main/webapp/dist";


/* === scss 解析压缩 === */

gulp.task("build-scss", function () {
    // 解析合并通用App Scss
    var appscss = _baseUrl + "/scss/**/*.scss";

    var combined = combiner.obj([
        gulp.src(appscss),
        $.sass(),
        $.cleanCss({
            compatibility: 'ie8',
            keepSpecialComments: 0
        }),
        $.autoprefixer({
            cascade: false
        }),
        gulp.dest(_dist + "/css"),
        $.filter('**/*.css'),
        browserSync.reload({ stream: true })
    ]);

    combined.on('error', console.error.bind(console));

    return combined;
});

/* === 压缩复制图片文件 === */

gulp.task("build-images", function () {
    var combined;
    combined = combiner.obj([
        gulp.src([_baseUrl + '/images/**/*'], {base: _baseUrl}),
        $.imagemin(),
        gulp.dest(_dist)
    ]);
    combined.on('error', console.error.bind(console));
    return combined;
});



/* === scripts 合并压缩 === */

// 合并通用App JS
var appjs = [
    _baseUrl + "/scripts/common/**/*.js",
    _baseUrl + "/scripts/app.js"
];
gulp.task("build-scripts-common", function () {
    var combined;

    combined = combiner.obj([
        gulp.src(appjs),
        $.cached('scripts'),
        $.order([
            "iotp.js",
            "iotp.toast.js",
            "iotp.messenger.js",
            "iotp.dialog.js",
            "iotp.ajax.js"
        ]),
        $.eslint(),
        $.eslint.format(),
        $.remember('scripts'),
        $.concat('app.js'),
        gulp.dest(_dist + "/scripts"),
        $.uglify(),
        $.rename({extname: '.min.js'}),
        gulp.dest(_dist + "/scripts")
    ]);

    combined.on('error', console.error.bind(console));
    return combined;
});

// 压缩各页面JS
var pagejs = [
    _baseUrl + "/scripts/components/**/*.js",
    _baseUrl + "/scripts/pages/**/*.js"
];
gulp.task("build-scripts-pages", function () {
    var combined;
    combined = combiner.obj([
        gulp.src(pagejs, {base: _baseUrl + '/scripts'}),
        $.cached('scripts-pages'),
        $.eslint(),
        $.eslint.format(),
        $.uglify(),
        $.remember('scripts-pages'),
        gulp.dest(_dist + "/scripts")
    ]);
    combined.on('error', console.error.bind(console));
    return combined;
});

// 压缩各页面JS
var vuejs = [ _baseUrl + "/scripts-vue/**/*.js" ];
gulp.task("build-scripts-vue", function () {
    var combined;
    combined = combiner.obj([
        gulp.src(vuejs, {base: _baseUrl + '/scripts-vue'}),
        $.cached('scripts-vue'),
        /*$.eslint(),
        $.eslint.format(),*/
        $.babel(),
        $.uglify(),
        $.remember('scripts-vue'),
        gulp.dest(_dist + "/scripts-vue")
    ]);
    combined.on('error', console.error.bind(console));
    return combined;
});

/* === 复制第三方脚本 === */

gulp.task("build-scripts-3rd", function () {
    var combined;
    combined = combiner.obj([
        gulp.src([
            _baseUrl + "/3rd/**/*",
            _baseUrl + "/3rd-vue/**/*",
            _baseUrl + "/fonts/**/*"
        ], {base: _baseUrl}),
        gulp.dest(_dist)
    ]);
    combined.on('error', console.error.bind(console));
    return combined;
});


// 合并第三方js
gulp.task("concat-scripts-plugins", function () {
    var combined;

    var vendorPath = _baseUrl + "/scripts-plugins/";

    combined = combiner.obj([
        gulp.src(vendorPath + "/**/*.js"),
        $.concat('vendor.js'),
        gulp.dest(_dist + "/scripts"),
        $.uglify(),
        $.rename({extname: '.min.js'}),
        gulp.dest(_dist + "/scripts")
    ]);

    combined.on('error', console.error.bind(console));

    combined = combiner.obj([
        gulp.src(vendorPath + "/**/*.css"),
        $.concat('vendor.css'),
        gulp.dest(_dist + "/css"),
        $.cleanCss({
            compatibility: 'ie8',
            keepSpecialComments: 0
        }),
        $.rename({extname: '.min.css'}),
        gulp.dest(_dist + "/css")
    ]);

    return combined;
});


/* === scripts 语法检测 === */

gulp.task("eslint", function () {
    return  gulp.src([_baseUrl + "/scripts/**/*.js"])
        .pipe($.eslint())
        .pipe($.eslint.format())
        .pipe($.eslint.failAfterError());
});


/* === 文件清理 === */

gulp.task("clean", function () {
    return del([ _dist + "/**/*" ]);
});


/* === 本地开发监听服务 === */

// 如果一个文件被删除了，则将其忘记
var forgetFile = function (event, name) {
    if (event.type === 'deleted') {
        delete $.cached.caches[event.path];
        $.remember.forget(name, event.path);
    }
};

gulp.task("dev", ["build-scss"], function () {
    browserSync.init({
        proxy: "localhost:8080",
        notify: false,
        //在Chrome浏览器中打开网站
        browser: "google chrome"
        //停止自动打开浏览器
        // open: false
    });

    // 监听SCSS变化
    gulp.watch(_baseUrl + "/scss/**/*.scss", ['build-scss'], function(event) {
        forgetFile(event, "scss");
    });

    // 监听JS文件修改
    gulp.watch(appjs, ['build-scripts-common'], function(event) {
        forgetFile(event, "scripts");
    });
    gulp.watch(pagejs, ['build-scripts-pages'], function(event) {
        forgetFile(event, "scripts-pages");
    });
    gulp.watch(vuejs, ['build-scripts-vue'], function(event) {
        forgetFile(event, "scripts-vue");
    });

    // 监听HTML文件修改
    gulp.watch([
        "./src/main/webapp/WEB-INF/views/**/*.html",
        _baseUrl + '/**/*.js'
    ])
        .on("change", browserSync.reload);
});


/* === 打包发布 === */

gulp.task("release", ['eslint'], function(){
    runSequence(
        'clean',
        'build-scss',
        'build-scripts-common',
        'build-scripts-pages',
        'build-scripts-vue',
        'build-scripts-3rd',
        'concat-scripts-plugins',
        'build-images'
    )
});

