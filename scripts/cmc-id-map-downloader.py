#!/bin/python
import json
import requests
import sys

API_ENDPOINT = 'https://files.coinmarketcap.com/generated/search/quick_search.json'

if __name__=='__main__':
    if len(sys.argv) != 2:
        print("USAGE: %s OUTPUT_FILE" % sys.argv[0])
        exit(1)
    out = {}
    call = requests.get(API_ENDPOINT)
    data = json.loads(call.text)
    for coin in data:
        out[coin['slug']] = coin['id']
    with open(sys.argv[1], 'w') as f:
        f.write(json.dumps(out))
