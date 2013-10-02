require 'vertx'

data = []

Vertx::FileSystem.read_file_as_buffer('/usr/share/dict/words') do |err, buf|
  raise err if err
  
  Vertx::RecordParser.new_delimited("\n") do |line|
    data << line.to_s
  end.input(buf)
end

Vertx.set_periodic(100) do
  Vertx::EventBus.publish(Vertx.config["stream-address"], data.sample)
end
