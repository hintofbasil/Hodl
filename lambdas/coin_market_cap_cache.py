"""
A AWS lambda to gather data on all CoinMarketCap coins
and format it so the app can easily injest it
"""

import boto3
import json
from botocore.vendored import requests

URL = 'https://api.coinmarketcap.com/v2/ticker/?start={}&limit={}&sort=id&structure=array'
s3 = boto3.resource('s3')

def get_single_response(index):
    start = index * 100
    limit = 100
    url = URL.format(start, limit)
    response = requests.get(url)
    return json.loads(response.text)['data']

def get_all_coins():
    coin_list = []
    for i in range(100):
        coin_sub_list = get_single_response(i)
        if not coin_sub_list:
            break
        coin_sub_list = list(map(lambda x: flatten_coin(x), coin_sub_list))
        coin_list = coin_list + coin_sub_list
    return coin_list

def flatten_coin(data):
    flattened = {}
    flattened['id'] = data['website_slug']
    flattened['name'] = data['name']
    flattened['symbol'] = data['symbol']
    flattened['rank'] = str(data['rank'])
    flattened['price_usd'] = str(data['quotes']['USD']['price'])
    return flattened

def write_file(data):
    return s3.Bucket('fixer-io-cache').Object('coin_market_cap_latest.json').put(Body=data).update()

def lambda_handler(event, context):
    coins = get_all_coins()
    coins = json.dumps(coins)
    write_file(coins)
    return {
        'statusCode': 200,
        'body': json.dumps('Success')
    }
