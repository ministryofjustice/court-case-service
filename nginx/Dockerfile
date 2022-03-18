FROM nginx:1.21.6-alpine

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx
RUN chown -R nginx:nginx /var/log/nginx
RUN touch /var/run/nginx.pid && chown -R nginx:nginx /var/run/nginx.pid

RUN apt update

COPY /nginx.conf /etc/nginx/
COPY /default.conf.template /etc/nginx/templates/

EXPOSE 8080
