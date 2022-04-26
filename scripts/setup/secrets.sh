oc process -f kameletbinding/secret-stage1.yaml \
-p RESOURCE_NAME=stage1 \
-p SOURCE_CLIENT_ID=your-client-id \
-p SOURCE_CLIENT_SECRET=your-client-secret \
| oc apply -f -