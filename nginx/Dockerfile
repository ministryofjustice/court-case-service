FROM nginx:1.21.0-alpine

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx

COPY /nginx.conf /etc/nginx/
COPY /default.conf /etc/nginx/conf.d/

EXPOSE 8080
