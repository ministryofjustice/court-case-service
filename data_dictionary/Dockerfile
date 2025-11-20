FROM nginx:1.29.3-alpine

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx
RUN chown -R nginx:nginx /var/log/nginx
RUN touch /var/run/nginx.pid && chown -R nginx:nginx /var/run/nginx.pid

RUN apk upgrade --no-cache

COPY /nginx.conf /etc/nginx/
COPY schema/ /usr/share/nginx/html/meta/schema

EXPOSE 8080