FROM nginx:1.21.6-alpine

WORKDIR /app

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx
RUN chown -R nginx:nginx /var/log/nginx

RUN apk -U upgrade

COPY schema/ /usr/share/nginx/html/meta/schema