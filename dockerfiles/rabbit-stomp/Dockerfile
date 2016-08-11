FROM rabbitmq:3.6.0-management 
MAINTAINER Pablo Castelo <pablo.castelo@redb.ee> 
RUN rabbitmq-plugins enable rabbitmq_stomp --offline 
RUN rabbitmq-plugins enable rabbitmq_stomp 
RUN rabbitmq-plugins enable rabbitmq_web_stomp 
EXPOSE 61613 
EXPOSE 15674%  
