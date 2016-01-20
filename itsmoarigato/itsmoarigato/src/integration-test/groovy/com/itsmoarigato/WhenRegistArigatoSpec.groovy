package com.itsmoarigato

import groovy.json.*

import geb.spock.GebReportingSpec;

import com.itsmoarigato.pages.*

class WhenRegistArigatoSpec extends GebReportingSpec {
	def "ちょっとやってみた"(){
		when: 'firstAccess'
			via HomePage
		then:
			at LoginPage
		
		when: 'log in successfully'
			login()
		then: 'sent to original page'
			at HomePage
		and: 'the username is displayed'
			username == 'takashi'
			
		when: "rest list access nodata"	
			go "http://localhost:8080/rest/arigato"
		then:
			$("pre").text().startsWith("[")

		when: "ありがとを登録すると"	
			via CreatePage
			"項目を入力して登録する"()
		then: "ホームページが表示されるべき"
			at HomePage
		when:"rest list access one data"	
			go "http://localhost:8080/rest/arigato"
			waitFor { $("pre").text().startsWith("[") }
		then: 
			def slurper = new JsonSlurper()
			def root = slurper.parseText($("pre").text())
			root.size >= 1
			def arigatoId = root[0]['id']
			root[0]['fromUser']['email'] == "takashi@hoge.co.jp"
			root[0]['toUser']['email'] == "bucho@hoge.co.jp"
			root[0]['subject'] == "いつもありがと"
			root[0]['contents'] == "今日も頑張ってるね:)"
			
		when: "登録したありがとを更新すると"	
			go UpdatePage.url + arigatoId
			at UpdatePage
			"項目を入力して更新する"()
		then: "更新されました!と表示されるべき"()
		when: "登録したありがとを表示すると"	
			go "http://localhost:8080/rest/arigato/"+arigatoId
		and:
			root = slurper.parseText($("pre").text())
		then: 
			root['fromUser']['email'] == "takashi@hoge.co.jp"
			root['toUser']['email'] == "bucho@hoge.co.jp"
			root['subject'] == "今日もありがと"
			root['contents'] == "ムリしないでね:)"
	}

	def "入力に誤りがある場合"(){
		when: 'firstAccess'
			go "http://localhost:8080"
		then:
			at LoginPage
		
		when: 'log in successfully'
			login()
		then: 'sent to original page'
			at HomePage
		when:"rest list access one data"	
			go "http://localhost:8080/"
			go "http://localhost:8080/rest/arigato"
			waitFor { $("pre").text().startsWith("[") }
		then: 
			def slurper = new JsonSlurper()
			def root = slurper.parseText($("pre").text())
			root.size >= 1
			def arigatoId = root[0]['id']

		when: "ありがとを入力ミスで登録すると"	
			go "http://localhost:8080/create"
			$('#submit').click()
			waitFor{ $('#errors').text() != "" }
		then: "Validation failed!と表示されるべき"
			$('#errors').text() == "入力エラーがあります。ご確認ください"
			$('#toUserId_error').text() == "入力してください"
			$('#subject_error').text() == "入力してください"
			$('#message_error').text() == "入力してください"

		when: "ありがとを入力ミスで更新すると"
			go "http://localhost:8080/update/"+arigatoId
			$('#toUserId').value("") 
			$('#subject').value("")
			$('#message').value("")
			$('#submit').click()
			waitFor{ $('#errors').text() != "" }
		then: "Validation failed!と表示されるべき"
			$('#errors').text() == "入力エラーがあります。ご確認ください"
			$('#subject_error').text() == "入力してください"
			$('#message_error').text() == "入力してください"
	}
	def "画面遷移のテスト"(){
		given: "ログインした状態で"
			go "http://localhost:8080"
			at LoginPage
			login()
			at HomePage
		when: "新規登録リンクをクリックする"()
		then: "新規登録画面が表示されるべき"
			at CreatePage
		when: "項目を入力して登録する"()
		then: "メイン画面が表示されるべき"
			at HomePage
		when: "最初のメッセージの更新をクリックする"()
		then: "更新画面が表示されるべき"
			at UpdatePage
		when: "項目を入力して更新する"()
		then: "更新画面が表示されるべき"
			at UpdatePage
		when: "自分のアカウントをクリックする"
			//FIXME "自分のアカウントをクリックする"() が Element is not clickable at point (589, 0). Other element would receive the click:となるので暫定措置
			via MyPage
		then: "自分が書いたメッセージの一覧が表示されるべき"
			at MyPage
		when: "最初のメッセージの更新をクリックする"()
		then: "更新画面が表示されるべき"
			at UpdatePage
		when: "項目を入力して更新する"()
		then: "更新画面が表示されるべき"
			at UpdatePage
			
		when: "メイン画面を表示する"
			via HomePage
			"部長のアイコンをクリックする"()
		then: "部長のページに遷移するべき"
			at FriendPage
		when: "新規登録リンクをクリックする"()
		then: "新規登録画面が表示されるべき"
			at CreatePage
		then: "toUserIdのテキストボックスの値が部長のidであるべき"()
		
	}

	def "削除のテスト"(){
		given: "ログインした状態で"
			go "http://localhost:8080"
			at LoginPage
			login()
			at HomePage
		when: "新規登録リンクをクリックする"()
		then: "新規登録画面が表示されるべき"
			at CreatePage
		when: "項目を入力して登録する"('delete test')
		then: "メイン画面が表示されるべき"
			at HomePage
		when: "最初のメッセージの削除をクリックする"()
		then: "最初のメッセージの件名が'delete test'でないべき"()
		when: "新規登録リンクをクリックする"()
		then: "新規登録画面が表示されるべき"
			at CreatePage
		when: "項目を入力して登録する"('delete test')
		then: "メイン画面が表示されるべき"
			at HomePage
		when: "自分のアカウントをクリックする"
			//FIXME "自分のアカウントをクリックする"() が Element is not clickable at point (589, 0). Other element would receive the click:となるので暫定措置
			via MyPage
		then: "自分が書いたメッセージの一覧が表示されるべき"
			at MyPage
		when: "最初のメッセージの削除をクリックする"()
		then: "最初のメッセージの件名が'delete test'でないべき"()
	}
}
