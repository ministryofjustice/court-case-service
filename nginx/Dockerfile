FROM nginx:1.21.0-alpine

RUN chown -R nginx:nginx /etc/nginx
RUN chown -R nginx:nginx /var/cache/nginx
RUN chown -R nginx:nginx /var/log/nginx
RUN touch /var/run/nginx.pid && chown -R nginx:nginx /var/run/nginx.pid

# Address vulnerability: https://snyk.io/vuln/SNYK-ALPINE313-LIBXML2-1300261
RUN apk upgrade libxml2

COPY /nginx.conf /etc/nginx/
COPY /default.conf.template /etc/nginx/templates/

EXPOSE 8080
