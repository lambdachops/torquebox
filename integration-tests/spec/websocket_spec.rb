# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'spec_helper'
require 'websocket-client-simple'

feature "basic rack websockets" do

  torquebox("--dir" => "#{apps_dir}/rack/websockets",
            "--context-path" => "/websockets",
            "-e" => "production")

  # TODO: Get raw WebSockets working from inside WildFly
  if embedded_from_disk?
    it "should work for basic requests" do
      uri = URI.parse(Capybara.app_host)
      ws = WebSocket::Client::Simple.connect("ws://#{uri.host}:#{uri.port}/websockets/ws")
      latch = java.util.concurrent.CountDownLatch.new(1)
      message = ""
      ws.on :open do
        ws.send("foobarbaz")
      end
      ws.on :message do |msg|
        message = msg.data
        latch.count_down
      end
      ws.on :error do |e|
        puts "ERROR: #{e}"
      end
      latch.await(10, java.util.concurrent.TimeUnit::SECONDS)
      ws.close
      if message.empty?
        puts ws.inspect
      end
      message.should == "foobarbaz"
    end
  end
end
