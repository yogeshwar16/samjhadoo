#!/bin/bash

# Create required directories
mkdir -p monitoring/grafana/provisioning/datasources

# Create Grafana data source configuration
cat > monitoring/grafana/provisioning/datasources/datasource.yml << 'EOL'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    version: 1
    editable: true
    jsonData:
      timeInterval: "5s"
EOL

# Create Prometheus alert manager configuration
mkdir -p monitoring/prometheus/alertmanager

cat > monitoring/prometheus/alertmanager/config.yml << 'EOL'
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alerts@samjhadoo.com'
  smtp_auth_username: '${SMTP_USER}'
  smtp_auth_password: '${SMTP_PASSWORD}'
  smtp_require_tls: true

route:
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 5m
  repeat_interval: 3h
  receiver: 'email-and-slack'
  routes:
    - match:
        severity: 'critical'
      receiver: 'pagerduty'

receivers:
  - name: 'email-and-slack'
    email_configs:
      - to: 'dev-team@samjhadoo.com'
        send_resolved: true
    slack_configs:
      - api_url: '${SLACK_WEBHOOK_URL}'
        channel: '#alerts'
        send_resolved: true
        title: '{{ template "slack.default.title" . }}'
        text: '{{ template "slack.default.text" . }}'
        title_link: '{{ template "slack.default.titlelink" . }}'
  
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: '${PAGERDUTY_KEY}'
        description: '{{ .CommonAnnotations.summary }}'
        details:
          firing: '{{ .Alerts.Firing | len }}'
          resolved: '{{ .Alerts.Resolved | len }}'
          summary: '{{ .CommonAnnotations.summary }}'
          description: '{{ .CommonAnnotations.description }}'
EOL

# Update Prometheus configuration to include alert rules
echo "Adding alert rules to Prometheus configuration..."
cat >> monitoring/prometheus/prometheus.yml << 'EOL'

rule_files:
  - 'alert.rules'

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']
EOL

echo "Monitoring setup complete!"
echo "To start the monitoring stack, run:"
echo "docker-compose -f docker-compose.monitoring.yml up -d"
