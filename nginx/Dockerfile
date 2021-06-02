FROM nginx:1.21.0-alpine

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx
RUN chown -R nginx:nginx /var/log/nginx
RUN touch /var/run/nginx.pid && chown -R nginx:nginx /var/run/nginx.pid

COPY /nginx.conf /etc/nginx/
COPY /conf.d/default.conf /etc/nginx/conf.d/

EXPOSE 8080
