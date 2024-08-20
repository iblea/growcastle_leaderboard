#!/bin/bash


APIBaseURL="https://raongames.com/growcastle/restapi/season/"

# current_date=$(TZ=Asia/Seoul date +"%Y-%m-%d")
# echo "kst current date : $current_date"

current_date="now"

PlayerURL="https://raongames.com/growcastle/restapi/season/${current_date}/players"

# curl -vsk --http1.1 --request GET --url "$PlayerURL"
# exit 0

response=$(curl -sk --http1.1 --request GET --url "$PlayerURL")

# echo "$response"

echo "$response" | jq '.result.date'
echo "=================================="
echo "$response" | jq '.result.list[] | select(.name=="Ib")'
