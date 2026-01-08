#!/bin/bash
lein run && rsync -avz --delete public/ xuh@8.159.137.181:/var/www/my-blog/
