#!/bin/sh

readonly BUCKET="asm.thi.ng"

s3cmd -P put resources/public/index.html s3://$BUCKET/
s3cmd -P put resources/public/css/style.css s3://$BUCKET/css/
s3cmd -P put resources/public/css/highlight/monokai-sublime.css s3://$BUCKET/css/highlight/
s3cmd -P put resources/public/js/*.js s3://$BUCKET/js/
s3cmd -P put resources/public/favicon.ico s3://$BUCKET/
